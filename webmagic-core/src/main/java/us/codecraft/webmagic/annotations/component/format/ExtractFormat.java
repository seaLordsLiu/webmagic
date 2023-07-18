package us.codecraft.webmagic.annotations.component.format;

import us.codecraft.webmagic.Page;

import java.util.List;

/**
 * @author liu xw
 * @date 2023 36-18
 */
public interface ExtractFormat {

    /**
     * 解析配置
     * @param sourceList 源数据
     * @param page 界面信息
     * @return 结果
     */
    List<String> format(List<String> sourceList, Page page);
}
