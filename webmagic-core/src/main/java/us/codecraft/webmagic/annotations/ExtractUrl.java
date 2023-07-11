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
public @interface ExtractUrl {
    /**
     * 提取类型
     * @return 默认是html
     */
    Extract extract();

    /**
     * URL解析的Model
     */
    Class<?> modelClass();

    /**
     * 地址过滤器 - 正则表达式
     * @return 结果
     */
    String filterRegEx() default "";
}
