package us.codecraft.webmagic.annotations.component.format;

import us.codecraft.webmagic.Page;

import java.util.List;

/**
 * @author liu xw
 * @date 2023 36-18
 */
public class DefaultExtractFormat implements ExtractFormat{

    @Override
    public List<String> format(List<String> sourceList, Page page) {
        return sourceList;
    }
}
