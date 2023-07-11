package us.codecraft.webmagic.annotations.metadata;

import us.codecraft.webmagic.annotations.Filter;

/**
 * @author liu xw
 * @date 2023 55-11
 */
public class AutoFilter implements Filter {

    @Override
    public Boolean filter(String url) {
        return Boolean.TRUE;
    }
}
