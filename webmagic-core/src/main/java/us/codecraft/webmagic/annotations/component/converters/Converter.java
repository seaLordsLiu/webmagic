package us.codecraft.webmagic.annotations.component.converters;

import us.codecraft.webmagic.annotations.Extract;
import us.codecraft.webmagic.annotations.metadata.FieldMetadata;

import java.util.List;

/**
 * 配置给 {@link Extract} 处理字段的转化器
 * @author liu xw
 * @date 2023 07-06
 */
public interface Converter<T> {

    /**
     * 转化为 java的数据信息
     * @param originalData 从页面中抓取的数据
     * @return 转化后的数据
     */
    T convertToJavaData(List<String> originalData, FieldMetadata metadata);
}
