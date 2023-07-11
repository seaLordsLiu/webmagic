package us.codecraft.webmagic;

import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.AnnotationsPageProcessor;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.Scheduler;
import us.codecraft.webmagic.scheduler.ThreadLocalQueueScheduler;

/**
 * @author liu xw
 * @date 2023 07-03
 */
public class SpiderFactory {

    /**
     * 下载处理器
     */
    private static final Downloader DOWNLOADER = new HttpClientDownloader();

    /**
     * 消息调度器
     */
    private static final Scheduler SCHEDULER = new ThreadLocalQueueScheduler();

    /**
     * 注解处理器
     */
    private static final PageProcessor PROCESSOR = new AnnotationsPageProcessor();

    public static Spider createSpider(Pipeline pipeline){
        return new Spider(pipeline, DOWNLOADER, PROCESSOR, SCHEDULER);
    }
}
