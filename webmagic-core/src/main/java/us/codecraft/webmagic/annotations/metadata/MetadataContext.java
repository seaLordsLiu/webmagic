package us.codecraft.webmagic.annotations.metadata;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.annotations.metadata.annotation.AnnotationMetadata;

import java.util.List;

/**
 * 元数据中心上下文内容
 * 理解为是一个类的元数据上下文内容
 * @author liu xw
 * @date 2023 07-07
 */
public interface MetadataContext {

    /**
     * 获取绑定类的信息
     */
    Class<?> getContextClass();

    /**
     * 获取绑定类的实例
     * 每次都是新的实例
     */
    Object getContextObject();

    /**
     * 获取字段的注解元数据信息
     */
    List<FieldMetadata> metadataOnField();

    /**
     * 获取类的注解元数据信息
     */
    ClassMetadata metadataOnClass();
}
