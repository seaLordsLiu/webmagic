package us.codecraft.webmagic;

import lombok.Getter;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.AnnotationsPageProcessor;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.Scheduler;
import us.codecraft.webmagic.scheduler.ThreadLocalQueueScheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author liu xw
 * @date 2023 07-03
 */
public class SpiderFactory {

    /**
     * 执行器缓存
     */
    private static final Map<String, Spider> SPIDER_CACHE = new ConcurrentHashMap<>();

    /**
     * 创建
     */
    public static Spider createSpider(Pipeline pipeline){
        return createSpider(pipeline, null);
    }

    /**
     * 创建
     * 补充消费 spiderConsumer 可以用来设置参数
     */
    public static Spider createSpider(Pipeline pipeline, Consumer<Site> siteConsumer){
        final Spider spider = new Spider(pipeline);
        if (siteConsumer != null){
            siteConsumer.accept(spider.getSite());
        }
        SPIDER_CACHE.put(spider.getUUID(), spider);
        return spider;
    }



    public static void cloneSpider(String trance){
        SPIDER_CACHE.remove(trance);
    }

    public static Spider getSpider(String trance){
        return SPIDER_CACHE.getOrDefault(trance, null);
    }


}
