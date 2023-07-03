package us.codecraft.webmagic.processor.example;

import us.codecraft.webmagic.*;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @author code4crafter@gmail.com <br>
 * @since 0.4.0
 */
public class BaiduBaikePageProcessor implements PageProcessor {

    private Site site = Site.me()//.setHttpProxy(new HttpHost("127.0.0.1",8888))
            .setRetryTimes(3).setSleepTime(1000).setUseGzip(true);

    @Override
    public void process(PageResult result, Page page) {
        result.getResultItems().put("name", page.getHtml().css("dl.lemmaWgt-lemmaTitle h1","text").toString());
        result.getResultItems().put("description", page.getHtml().xpath("//div[@class='lemma-summary']/allText()"));
        result.getResultItems().setSkip(Boolean.FALSE);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) throws InterruptedException {
        //single download
        SpiderFactoryImpl factory = new SpiderFactoryImpl();

        Spider spider = factory.getSpider(new BaiduBaikePageProcessor());

        String urlTemplate = "http://baike.baidu.com/search/word?word=%s&pic=1&sug=1&enc=utf8";
        spider.start(String.format(urlTemplate, "水力发电"));


        Thread.sleep(1000000L);

    }
}
