package us.codecraft.webmagic;

import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @author liu xw
 * @date 2023 07-03
 */
public interface SpiderFactory {

    /**
     * 普通的爬虫程序
     * @param processor 界面解析器
     */
    Spider getSpider(PageProcessor processor, Pipeline pipeline);

    Spider getSpider(PageProcessor processor);
}
