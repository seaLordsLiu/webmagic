package us.codecraft.webmagic;

import lombok.Data;
import org.junit.Test;
import us.codecraft.webmagic.annotations.Extract;
import us.codecraft.webmagic.annotations.ExtractUrl;
import us.codecraft.webmagic.emus.ExtractTypeEnum;
import us.codecraft.webmagic.pipeline.ConsolePipeline;

/**
 * @author liu xw
 * @date 2023 07-07
 */
public class SpiderTest {


    @Test
    public void db(){
        Spider spider = SpiderFactory.createSpider(new ConsolePipeline());
        // 豆瓣检索 支持按照书名称、isbn、作者进行查询
        spider.start("https://book.douban.com/j/subject_suggest?q=鲁迅", SpiderTest.DB.class);
    }


    /**
     * [
     *     {
     *         "title": "后电影视觉",
     *         "url": "https:\/\/book.douban.com\/subject\/36282018\/",
     *         "pic": "https://img9.doubanio.com\/view\/subject\/s\/public\/s34562736.jpg",
     *         "author_name": "（美）罗杰·F.库克",
     *         "year": "2023",
     *         "type": "b",
     *         "id": "36282018"
     *     }
     * ]
     */
    @Data
    @ExtractUrl(
        extract = @Extract(value = "$..url", type = ExtractTypeEnum.JSON),
        modelClass = SpiderTest.BookDB.class,
        filterRegEx = "https://book.douban.com/subject/\\d+/"
    )
    public static class DB{

    }


    @Data
    public static class BookDB{

            /**
             * 标签
             */
            @Extract(value = "//head/meta[@property='og:title']/@content", type = ExtractTypeEnum.HTML)
            private String title;

            /**
             * 简介
             */
            @Extract(value = "//head/meta[@property='og:description']/@content", type = ExtractTypeEnum.HTML)
            private String description;

            /**
             * 时间
             */
            @Extract(value = "//div[@id='info']/span[contains(:text:one,'出版年:')]/followingSiblingText()")
            private String date;
    }
}