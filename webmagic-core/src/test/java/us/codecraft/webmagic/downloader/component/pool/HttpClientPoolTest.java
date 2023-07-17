package us.codecraft.webmagic.downloader.component.pool;

import junit.framework.TestCase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import us.codecraft.webmagic.Site;

import java.io.IOException;

/**
 * @author liu xw
 * @date 2023 47-16
 */
public class HttpClientPoolTest {

    HttpClientPool pool = new HttpClientPool();

    @Test
    public void httpClientHeartbeatTest() throws IOException {
        CloseableHttpClient httpClient = pool.getHttpClient(new Site().setDomain("123"));
//        httpClient.close();


        CloseableHttpClient httpClient1 = pool.getHttpClient(new Site().setDomain("123"));
        httpClient.close();
    }
}