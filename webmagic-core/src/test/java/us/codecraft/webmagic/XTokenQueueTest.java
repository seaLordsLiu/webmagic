package us.codecraft.webmagic;

import us.codecraft.xsoup.XTokenQueue;
import us.codecraft.xsoup.xevaluator.XPathParser;

/**
 * @author liu xw
 * @date 2023 07-09
 */
public class XTokenQueueTest {


    static XPathParser parser = new XPathParser("[contains(@text(),'出版年:')]");


    public static void main(String[] args) {
        parser.parse();
    }
}
