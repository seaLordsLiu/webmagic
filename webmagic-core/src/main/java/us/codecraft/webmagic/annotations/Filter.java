package us.codecraft.webmagic.annotations;

/**
 * @author liu xw
 * @date 2023 51-11
 */
@FunctionalInterface
public interface Filter {


    /**
     * 过滤器
     * @param url 地址
     * @return TRUE 添加 FALSE 不添加
     */
    Boolean filter(String url);
}
