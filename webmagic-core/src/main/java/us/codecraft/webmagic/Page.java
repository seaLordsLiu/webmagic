package us.codecraft.webmagic;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Json;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.utils.HttpConstant;
import us.codecraft.webmagic.utils.UrlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Object storing extracted result and urls to fetch.<br>
 * Not thread safe.<br>
 * Main method：                                               <br>
 * {@link #getUrl()} get url of current page                   <br>
 * {@link #getHtml()}  get content of current page                 <br>
 *
 * @author code4crafter@gmail.com <br>
 * @see Downloader
 * @see us.codecraft.webmagic.processor.PageProcessor
 * @since 0.1.0
 */
@Data
@RequiredArgsConstructor
public class Page {

    private final Request request;

    private Html html;

    private Json json;

    private String rawText;

    private Selectable url;

    private Map<String,List<String>> headers;

    private int statusCode = HttpConstant.StatusCode.CODE_200;

    private boolean downloadSuccess = true;

    private byte[] bytes;

    private String charset;

    public static Page fail(Request request){
        Page page = new Page(request);
        page.setDownloadSuccess(false);
        return page;
    }

    /**
     * get html content of page
     *
     * @return html
     */
    public Html getHtml() {
        if (html == null) {
            html = new Html(rawText, request.getUrl());
        }
        return html;
    }

    /**
     * get json content of page
     *
     * @return json
     * @since 0.5.0
     */
    public Json getJson() {
        if (json == null) {
            json = new Json(rawText);
        }
        return json;
    }
}