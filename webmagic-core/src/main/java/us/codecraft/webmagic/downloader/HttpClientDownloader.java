package us.codecraft.webmagic.downloader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.component.pool.HttpClientPool;
import us.codecraft.webmagic.downloader.component.proxy.Proxy;
import us.codecraft.webmagic.downloader.component.proxy.ProxyProvider;
import us.codecraft.webmagic.downloader.component.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.CharsetUtils;
import us.codecraft.webmagic.utils.HttpClientUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 用来代替HttpClientDownloader的类
 * @Author liu xw
 */
@Slf4j
@RequiredArgsConstructor
public class HttpClientDownloader implements Downloader {

    /**
     * 连接池
     */
    private final HttpClientPool clientPool = new HttpClientPool();

    /**
     * 请求信息转化
     */
    private final HttpUriRequestConverter httpUriRequestConverter = new HttpUriRequestConverter();

    /**
     * 代理信息
     */
    private ProxyProvider proxyProvider;

    public void setProxyProvider(ProxyProvider proxyProvider) {
        this.proxyProvider = proxyProvider;
    }


    @Override
    public void setThread(int thread) {
        clientPool.initHttpClientConnectionSize(thread);
    }

    /**
     * 下载界面信息
     */
    @Override
    public Page download(Request request, Task task) {
        if (task == null || task.getSite() == null) {
            throw new NullPointerException("task or site can not be null");
        }
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient httpClient = clientPool.getHttpClient(task.getSite());

        Proxy proxy = proxyProvider != null ? proxyProvider.getProxy(request, task) : null;

        // 构造请求对象
        HttpClientRequestContext requestContext = httpUriRequestConverter.convert(request, task.getSite(), proxy);

        // 下载
        Page page = Page.fail(request);
        try {
            httpResponse = httpClient.execute(requestContext.getHttpUriRequest(), requestContext.getHttpClientContext());
            // 回填写requestMethod
            if (StringUtils.isBlank(request.getMethod())){
                request.setMethod(requestContext.getHttpUriRequest().getMethod());
            }
            page = handleResponse(request, request.getCharset() != null ? request.getCharset() : task.getSite().getCharset(), httpResponse, task);
            return page;
        } catch (IOException e) {
            log.error("download page {} error", request.getUrl(), e);
            return page;
        } finally {
            if (httpResponse != null) {
                //ensure the connection is released back to pool
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
            if (proxyProvider != null && proxy != null) {
                proxyProvider.returnProxy(proxy, page, task);
            }
        }
    }

    private Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task) throws IOException {
        byte[] bytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
        String contentType = httpResponse.getEntity().getContentType() == null ? "" : httpResponse.getEntity().getContentType().getValue();
        Page page = new Page(request);
        page.setBytes(bytes);
        if (!request.isBinaryContent()) {
            if (charset == null) {
                charset = getHtmlCharset(contentType, bytes);
            }
            page.setCharset(charset);
            page.setRawText(new String(bytes, charset));
        }
        page.setUrl(new PlainText(request.getUrl()));
        page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        page.setDownloadSuccess(true);
        page.setHeaders(HttpClientUtils.convertHeaders(httpResponse.getAllHeaders()));
        return page;
    }

    private String getHtmlCharset(String contentType, byte[] contentBytes) throws IOException {
        String charset = CharsetUtils.detectCharset(contentType, contentBytes);
        if (charset == null) {
            charset = Charset.defaultCharset().name();
            log.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", Charset.defaultCharset());
        }
        return charset;
    }
}
