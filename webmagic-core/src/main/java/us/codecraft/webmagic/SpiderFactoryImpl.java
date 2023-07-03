package us.codecraft.webmagic;

import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.Scheduler;
import us.codecraft.webmagic.scheduler.ThreadLocalQueueScheduler;

/**
 * @author liu xw
 * @date 2023 07-03
 */
public class SpiderFactoryImpl implements SpiderFactory {

    /**
     * 下载处理器
     */
    private final Downloader downloader = new HttpClientDownloader();

    /**
     * 消息调度器
     */
    private final Scheduler scheduler = new ThreadLocalQueueScheduler();

    @Override
    public Spider getSpider(PageProcessor processor, Pipeline pipeline) {
        return new Spider(downloader, pipeline, processor, scheduler);
    }

    @Override
    public Spider getSpider(PageProcessor processor) {
        return getSpider(processor, new ConsolePipeline());
    }
}