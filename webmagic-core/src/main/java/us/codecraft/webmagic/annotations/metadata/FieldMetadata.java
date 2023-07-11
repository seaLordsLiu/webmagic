package us.codecraft.webmagic.annotations.metadata;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import us.codecraft.webmagic.annotations.metadata.annotation.AbstractAnnotationMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 字段的元数据信息
 * @author liu xw
 * @date 2023 07-07
 */
public class FieldMetadata extends AbstractAnnotationMetadata {

    @Getter
    private final Field field;

    public FieldMetadata(Field field) {
        this.field = field;

        // 初始化字段存储信息
        Annotation[] annotations = field.getDeclaredAnnotations();
        if (ArrayUtils.isEmpty(annotations)){
            return;
        }

        // 添加注解信息
        for (Annotation annotation : annotations) {
            getAnnotationCache().put(annotation.annotationType().getName(), annotation);
        }
    }
}
