package us.codecraft.webmagic.annotations.component;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.annotations.metadata.annotation.AnnotationMetadata;

/**
 * 提取解析器
 * @author liu xw
 * @date 2023 07-06
 */
public interface ExtractParser<T extends AnnotationMetadata> {

    /**
     * 解析数据信息
     * 方法目的：通过FieldMetadata记录的字段信息，解析页面信息，返回解析后的数据
     *
     * @param metadata 字段元数据信息
     * @param page     页面信息
     */
    Object parser(T metadata, Page page);
}
