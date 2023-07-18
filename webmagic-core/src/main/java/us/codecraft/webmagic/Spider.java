package us.codecraft.webmagic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import us.codecraft.webmagic.constants.SpiderConstants;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.Scheduler;
import us.codecraft.webmagic.scheduler.ThreadLocalQueueScheduler;
import us.codecraft.webmagic.thread.CountableThreadPool;
import us.codecraft.webmagic.thread.NameThreadFactory;
import us.codecraft.webmagic.utils.HttpConstant;
import us.codecraft.webmagic.utils.UrlUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 爬虫程序核心执行器
 * 工作意义：协调四大组件之间的调度关系
 * 1. 从scheduler组件中获取任务节点
 * 2. 使用downloader组件下载任务节点界面信息
 * 3. 通过配置的pageProcessor组件解析界面信息
 * 4. 配置的pipeline组件处理解析后的结果
 * @author liu xw
 * @date 2023 07-03
 */
@Slf4j(topic = "spider")
@RequiredArgsConstructor
public class Spider implements Runnable, Task {

    /**
     * 核心组件 - 结果处理器
     */
    protected final Pipeline pipeline;

    /**
     * 核心组件 - 下载组件
     */
    @Getter
    @Setter
    protected  Downloader downloader = SpiderFactory.component.downloader;

    /**
     * 核心组件 - 界面解析器
     */
    @Getter
    @Setter
    protected PageProcessor pageProcessor = SpiderFactory.component.pageProcessor;

    /**
     * 核心组件 - 消息调度器
     */
    @Getter
    @Setter
    protected Scheduler scheduler = SpiderFactory.component.scheduler;

    /**
     * 入口请求
     */
    @Setter
    private Request entranceRequest;

    public void start(String url, Class<?> modelClass){
        entranceRequest = new Request(url, modelClass);
        this.run();
    }

    /**
     * 当前爬虫任务的节点信息
     */
    private Site site;

    /**
     * 执行 - 唯一标识
     */
    private String trance;

    /**
     * 执行状态
     */
    protected AtomicInteger stat = new AtomicInteger(SpiderConstants.STAT_INIT);;

    /**
     * 执行次数
     */
    private final AtomicLong pageCount = new AtomicLong(0);


    /**
     * 执行任务 - 线程数量
     */
    @Setter
    @Getter
    private Integer countableThreadNum = 1;

    /**
     * 执行任务信息
     */
    @Getter
    private CountableThreadPool threadPool = null;


    /**
     * 初始化程序
     */
    private void init(){
        // 初始化 - 执行任务
        stat.set(SpiderConstants.STAT_RUNNING);

        // 初始化执行线程池 - 固定数量线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                countableThreadNum,
                countableThreadNum,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()
//                , new NameThreadFactory(getUUID() + "$"),
        );

        threadPool = new CountableThreadPool(countableThreadNum, threadPoolExecutor);

        // 补充入口请求信息
        this.addRequest(entranceRequest);
    };



    /**
     * 执行任务信息
     */
    @Override
    public void run() {
        final boolean enabled = log.isDebugEnabled();
        log.info("[{}] -> 开启执行任务. 入口地址: [{}]", this.getUUID(), entranceRequest.getUrl());
        final LocalDateTime startTime = LocalDateTime.now();
        init();
        try {
            // 获取一个 使用线程处理 request 请求任务. 每获取到一个非空的 request 时. 就会执行一个线程去运行
            while (SpiderConstants.STAT_RUNNING == stat.get()){
                Request request = getScheduler().poll(this);
                // 获取请求为null. 判断程序是否执行完成
                if (request == null){
                    if (enabled){
                        log.debug("[{}] -> 队列任务为空, 开启关闭检测.", getUUID());
                    }
                    // 判断存活线程数是否为0
                    if (threadPool.getThreadAlive() == 0){
                        // 二次确认请求
                        request = getScheduler().poll(this);
                        if (request == null){
                            if (enabled){
                                log.debug("[{}] -> 队列任务为空, 采集线程数数为0, 关闭采集任务. ", getUUID());
                            }
                            // 任务执行完成. 中断程序
                            break;
                        }
                        // request 二次检测不为空. 去执行任务
                    }else {
                        // 存活线程数不为0. 其他任务还在执行, 直接中断当前执行线程
                        if (enabled){
                            log.debug("[{}] -> 队列任务为空, 存在执行采集线程", getUUID());
                        }
                        continue;
                    }
                }
                // 执行请求
                final Request executeRequest = request;
                threadPool.execute(() -> {
                    // 计数
                    pageCount.incrementAndGet();

                    // 记录执行时间
                    Instant startInstant = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();
                    try {
                        if (executeRequest.getMethod() == null){
                            // 默认GET
                            executeRequest.setMethod(HttpConstant.Method.GET);
                        }
                        // 执行任务
                        log.info("[{}] -> 执行任务 method: [{}]. url: [{}]", getUUID(), executeRequest.getMethod(), executeRequest.getUrl());
                        executeRequest(executeRequest);
                    }catch (Exception e){
                        log.error("[{}] -> 采集任务执行失败 method: [{}]. url: [{}]", getUUID(), executeRequest.getMethod(), executeRequest.getUrl(),e);
                    }finally {
                        // 执行间隔
                        log.info("[{}] -> 执行完成 url: [{}]. 执行时间: [{}]毫秒. 线程休眠: [{}]毫秒",
                                getUUID(),
                                executeRequest.getUrl(),
                                Duration.between(startInstant, LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()).toMillis(),
                                getSite().getSleepTime()
                        );
                        sleep(getSite().getSleepTime());
                    }
                });
            }
        }finally {
            destroy();
        }
        log.info("[{}] -> 任务执行完毕. 执行时间: [{}]秒. 下载界面数量: [{}]", getUUID(), Duration.between(startTime, LocalDateTime.now()).getSeconds(), pageCount.get());
    }

    protected void destroy(){
        // 执行完成 - 设置执行状态为停止
        stat.set(SpiderConstants.STAT_STOPPED);
        // 关闭执行线程
        threadPool.shutdown();
        // 清理缓存信息
        SpiderFactory.cloneSpider(getUUID());
        // 清理线程上下文信息
        ThreadLocalQueueScheduler.clearContext();
    }

    /**
     * 执行任务
     */
    private void executeRequest(Request request) {

        // 1. 下载界面信息
        Page page = downloaderRequest(request);

        // 过滤 状态码信息
        if (!getSite().getAcceptStatCode().contains(page.getStatusCode())) {
            // 配置的节点状态不包含当前界面信息时, 不进行界面处理
            return;
        }

        // 2. 开始解析数据信息
        PageResult pageResult = new PageResult();
        pageProcessor.process(pageResult, page);


        // 3. 处理结果
        pipelinesRequest(pageResult);
    }

    /**
     * 执行 downloader 下载任务
     * @param request 下载请求
     */
    private Page downloaderRequest(Request request) {
        Page page;
        if (null != request.getDownloader()){
            page = request.getDownloader().download(request,this);
        }else {
            page = downloader.download(request, this);
        }
        if (!page.isDownloadSuccess()){
            // 下载失败 - 重试处理 || 下载html. document节点失败 重试
            log.error("[{}] -> 下载失败 url: [{}]", getUUID(), request.getUrl());
            doCycleRetry(request);
        }
        if (page.getHtml().getDocument() == null){
            log.error("[{}] -> 界面 (Document is null) 信息下载失败. url: [{}]", getUUID(), request.getUrl());
            doCycleRetry(request);
        }
        return page;
    }

    /**
     * 处理结果信息
     */
    private void pipelinesRequest(PageResult pageResult){
        boolean enabled = log.isDebugEnabled();
        // 1. 添加孵化请求
        if (CollectionUtils.isNotEmpty(pageResult.getHatchReqs())){
            for (Request newRequest : pageResult.getHatchReqs()) {
                if (enabled) {
                    log.debug("[{}] -> 孵化子请求信息. 请求地址 url: [{}]. 解析类 extractClazz: [{}]", getUUID(), newRequest.getUrl(), newRequest.getExtractClazz().getName());
                }
                this.scheduler.push(newRequest, this);
            }
        }

        // 2. 处理结果信息
        if (!pageResult.getSkip() || pageResult.getExtract() == null) {
            return;
        }
        pipeline.process(pageResult.getExtract(), this);
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 重试机制
     * @param request 请求
     */
    private void doCycleRetry(Request request) {
        Integer cycleTriedTimesObject = Optional.ofNullable(request.getExtra(Request.CYCLE_TRIED_TIMES)).map(obj -> (Integer) obj).orElse(1);
        if (cycleTriedTimesObject > getSite().getCycleRetryTimes()){
            // 重试次数超过配置的重试次数
            log.error("请求 -> [{}]. 超过配置的重试次数 -> [{}]. 中断请求", request.getUrl(), cycleTriedTimesObject);
            return;
        }
        log.debug("请求 -> [{}]. 重试次数 -> [{}]", request.getUrl(), cycleTriedTimesObject++);
        addRequest(SerializationUtils.clone(request).putExtra(Request.CYCLE_TRIED_TIMES, cycleTriedTimesObject));
    }

    private void addRequest(Request request) {
        if (getSite().getDomain() == null && request != null && request.getUrl() != null) {
            getSite().setDomain(UrlUtils.getDomain(request.getUrl()));
        }
        scheduler.push(request, this);
    }

    @Override
    public String getUUID() {
        if (this.trance == null){
            this.trance = UUID.randomUUID().toString();
        }
        return this.trance;
    }

    @Override
    public Site getSite() {
        if (null == this.site){
            this.site = pageProcessor.getSite();
        }
        return this.site;
    }
}
