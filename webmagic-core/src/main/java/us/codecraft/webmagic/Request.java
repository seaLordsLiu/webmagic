package us.codecraft.webmagic;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.utils.Experimental;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Object contains url to crawl.<br>
 * It contains some additional information.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
@Data
@RequiredArgsConstructor
public class Request implements Serializable {

    private static final long serialVersionUID = 2062192774891352043L;

    public static final String CYCLE_TRIED_TIMES = "_cycle_tried_times";


    /**
     * 请求url
     */
    private final String url;

    /**
     * 提取类
     */
    private final Class<?> extractClazz;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 字符集
     */
    private String charset;

    /**
     * 请求体
     */
    private HttpRequestBody requestBody;

    /**
     * this req use this downloader
     */
    private Downloader downloader;

    /**
     * Store additional information in extras.
     */
    private Map<String, Object> extras;

    /**
     * cookies for current url, if not set use Site's cookies
     */
    private Map<String, String> cookies = new HashMap<String, String>();

    private Map<String, String> headers = new HashMap<String, String>();


    /**
     * When it is set to TRUE, the downloader will not try to parse response body to text.
     *
     */
    private boolean binaryContent = false;

    @SuppressWarnings("unchecked")
    public <T> T getExtra(String key) {
        if (extras == null) {
            return null;
        }
        return (T) extras.get(key);
    }

    public <T> Request putExtra(String key, T value) {
        if (extras == null) {
            extras = new HashMap<String, Object>();
        }
        extras.put(key, value);
        return this;
    }


}
