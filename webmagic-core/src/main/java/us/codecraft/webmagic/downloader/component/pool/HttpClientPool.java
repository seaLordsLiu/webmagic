package us.codecraft.webmagic.downloader.component.pool;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.thread.NameThreadFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liu xw
 * @date 2023 07-03
 */
@Slf4j
public class HttpClientPool {

    public HttpClientPool() {
        log.info("执行 HttpClient 心跳检测 间隙时间 -> {} min", 2);
        // 每两分钟执行
        executorService.schedule(this::httpClientHeartbeat, 2, TimeUnit.MINUTES);
    }

    /**
     * 请求客户端构造
     */
    private final HttpClientGenerator httpClientGenerator = new HttpClientGenerator();

    /**
     * 请求客户端缓存
     * key -> request.domain value -> httpClient
     */
    private final Map<String, HttpClientWrapper> httpClientCache = new ConcurrentHashMap<>();


    /**
     * 心跳检测线程
     */
    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, new NameThreadFactory("httpClientPool-heartbeat-scheduled"));;

    /**
     * 获取HTTP请求客户端
     * @param site 站点
     */
    public CloseableHttpClient getHttpClient(Site site) {
        if (site == null) {
            throw new IllegalArgumentException("site data is null");
        }
        final String domain = site.getDomain();
        if (httpClientCache.containsKey(domain)) {
            return httpClientCache.get(domain).getHttpClient();
        }
        HttpClientWrapper httpClient;
        synchronized (this){
            httpClient = httpClientCache.get(domain);
            if (httpClient == null) {
                httpClient = HttpClientWrapper.of(httpClientGenerator.getClient(site));
                httpClientCache.put(domain, httpClient);
            }
        }
        return httpClient.getHttpClient();
    }

    public void initHttpClientConnectionSize(int size){
        this.httpClientGenerator.setPoolSize(size);
    }

    /**
     * 心跳检测
     */
    private void httpClientHeartbeat() {
        try {
            final long executeTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
            if (httpClientCache.isEmpty()){
                return;
            }
            final boolean debugEnabled = log.isDebugEnabled();
            httpClientCache.forEach((key, clientWrapper) -> {
                if (executeTime > clientWrapper.getLastAccessTime() + 5 * 60) {
                    try {
                        // 超过5分钟没有操作
                        httpClientCache.remove(key, clientWrapper);
                        if (debugEnabled){
                            log.debug("close domain -> {}. last access time -> {}",key, LocalDateTime.ofInstant(Instant.ofEpochMilli(clientWrapper.getLastAccessTime()), ZoneId.systemDefault()));
                        }
                        // 调用close方法会导致连接池关闭
//                        clientWrapper.getHttpClient().close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }finally {
            executorService.schedule(this::httpClientHeartbeat, 2, TimeUnit.MINUTES);
        }
    }

    /**
     * 连接对象
     */
    private static class HttpClientWrapper{

        public HttpClientWrapper(CloseableHttpClient httpClient) {
            this.httpClient = httpClient;
            this.lastAccessTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
        }

        /**
         * 对象
         */
        @Getter
        private final CloseableHttpClient httpClient;

        /**
         * 调用时间
         */
        @Getter
        @Setter
        private volatile Long lastAccessTime;


        public static HttpClientWrapper of(CloseableHttpClient httpClient){
            return new HttpClientWrapper(httpClient);
        }
    }
}