package us.codecraft.webmagic.annotations;

import java.lang.annotation.*;

/**
 * 提取 URL 地址信息
 * @author liu xw
 * @date 2023 07-07
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited // 声明注解具有继承性
public @interface ExtractUrls {

    /**
     * 提取类型
     * @return 默认是html
     */
    ExtractUrl[] urls();
}
