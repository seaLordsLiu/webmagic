package us.codecraft.webmagic;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.constants.SpiderConstants;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.Scheduler;
import us.codecraft.webmagic.scheduler.ThreadLocalQueueScheduler;
import us.codecraft.webmagic.utils.UrlUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
@RequiredArgsConstructor
public class Spider implements Runnable, Task {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 核心组件 - 结果处理器
     */
    protected final Pipeline pipeline;

    /**
     * 核心组件 - 下载组件
     */
    protected final Downloader downloader;

    /**
     * 核心组件 - 界面解析器
     */
    protected final PageProcessor pageProcessor;

    /**
     * 核心组件 - 消息调度器
     */
    protected final Scheduler scheduler;


    public void start(String url, Class<?> modelClass){
        Request request = new Request(url, modelClass);
        this.addRequest(request);
        try {
            this.run();
        }finally {
            ThreadLocalQueueScheduler.clearContext();
        }
    }

    public void startAsync(String url, Class<?> modelClass){
        Request request = new Request(url, modelClass);
        this.addRequest(request);
        CompletableFuture<Void> future = CompletableFuture.runAsync(this);
        future.thenRun(ThreadLocalQueueScheduler::clearContext);
    }

    /**
     * 当前爬虫任务的节点信息
     */
    private Site site;

    /**
     * 执行状态
     */
    protected AtomicInteger stat = new AtomicInteger(SpiderConstants.STAT_INIT);;

    /**
     * 执行次数
     */
    private final AtomicLong pageCount = new AtomicLong(0);;

    /**
     * 执行任务信息
     */
    @Override
    public void run() {
        stat.set(SpiderConstants.STAT_RUNNING);
        Request request = this.scheduler.poll(this);
        while (SpiderConstants.STAT_RUNNING == stat.get() && request != null){

            // 执行任务
            executeRequest(request);

            // 执行完成获取下一个任务信息
            request = this.scheduler.poll(this);

        }
        // 停止
        stat.set(SpiderConstants.STAT_STOPPED);

        logger.info("Spider {} closed! {} pages downloaded.", getUUID(), pageCount.get());
    }

    /**
     * 执行任务
     */
    private void executeRequest(Request request) {
        logger.info("execute -> [{}-{}]", request.getMethod(), request.getUrl());
        try {
            // 1. 下载界面信息
            Page page = downloaderRequest(request);

            // 2. 解析界面信息
            if (!getSite().getAcceptStatCode().contains(page.getStatusCode())) {
                // 配置的节点状态不包含当前界面信息时, 不进行界面处理
                return;
            }
            PageResult pageResult = new PageResult();
            pageProcessor.process(pageResult, page);

            // 3. 处理结果
            pipelinesRequest(pageResult);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("execute request error -> {}. Request Data -> [{}]", e.getMessage(), request);
        }finally {
            pageCount.incrementAndGet();
            // 执行间隔
            sleep(getSite().getSleepTime());
        }
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
            // 下载失败 - 重试处理
            doCycleRetry(request);
        }
        return page;
    }

    /**
     * 处理结果信息
     */
    private void pipelinesRequest(PageResult pageResult){
        if (pageResult == null){
            return;
        }
        boolean enabled = logger.isDebugEnabled();

        // 1. 添加孵化请求
        if (CollectionUtils.isNotEmpty(pageResult.getHatchReqs())){
            for (Request newRequest : pageResult.getHatchReqs()) {
                if (enabled) {
                    logger.debug("补充请求信息 -> [{}]", newRequest.getUrl());
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
            logger.error("Thread interrupted when sleep",e);
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
            logger.warn("请求 -> [{}]. 超过配置的重试次数 -> [{}]", request.getUrl(), cycleTriedTimesObject);
            return;
        }
        cycleTriedTimesObject++;
        logger.info("请求 -> [{}]. 重试次数 -> [{}]", request.getUrl(), cycleTriedTimesObject);
        addRequest(SerializationUtils.clone(request).putExtra(Request.CYCLE_TRIED_TIMES, cycleTriedTimesObject));
        sleep(getSite().getRetrySleepTime());
    }

    private void addRequest(Request request) {
        if (getSite().getDomain() == null && request != null && request.getUrl() != null) {
            getSite().setDomain(UrlUtils.getDomain(request.getUrl()));
        }
        scheduler.push(request, this);
    }

    @Override
    public String getUUID() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Site getSite() {
        if (null == this.site){
            this.site = pageProcessor.getSite();
        }
        return this.site;
    }
}
