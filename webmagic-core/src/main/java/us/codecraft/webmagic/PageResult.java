package us.codecraft.webmagic;

import lombok.Data;
import lombok.NonNull;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.ResultItems;

import java.util.List;

/**
 * 界面解析结果
 * @author liu xw
 * @date 2023 07-03
 */
@Data
public class PageResult {

    public static PageResult of(@NonNull Request request){
        ResultItems items = new ResultItems();
        items.setRequest(request);
        items.setSkip(Boolean.TRUE);

        return new PageResult(items);
    }

    /**
     * 界面结果
     */
    private final ResultItems resultItems;

    /**
     * 提取到请求节点信息
     */
    private List<Request> extractReqs;
}
