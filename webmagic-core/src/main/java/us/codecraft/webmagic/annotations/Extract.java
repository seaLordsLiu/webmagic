package us.codecraft.webmagic.annotations;

import us.codecraft.webmagic.emus.ExtractTypeEnum;

import java.lang.annotation.*;

/**
 * 提取界面信息
 * @author liu xw
 * @date 2023 07-05
 */

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited // 声明注解具有继承性
public @interface Extract {

    /**
     * 提取类型
     * @return 默认是html
     */
    ExtractTypeEnum type() default ExtractTypeEnum.HTML;

    /**
     * 提取表达式
     * @return 默认是空
     */
    String value();

}