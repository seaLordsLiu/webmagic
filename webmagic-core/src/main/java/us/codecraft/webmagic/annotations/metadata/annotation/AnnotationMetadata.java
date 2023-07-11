package us.codecraft.webmagic.annotations.metadata.annotation;

import org.apache.commons.lang3.ObjectUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.util.Collections;
import java.util.Set;

/**
 * 注解元数据信息
 * @author liu xw
 * @date 2023 07-07
 */
public interface AnnotationMetadata {

    /**
     * 获取所有的注解信息
     */
    Set<String> getAllAnnotationName();


    /**
     * 获取注解信息
     * @param annotationName 注解名称
     * @return 注解信息
     */
    Annotation getAnnotation(String annotationName) throws AnnotationTypeMismatchException;

    /**
     * 判断有没有注解
     * @param annotationName 注解类型名称
     * @return true 有 false 没有
     */
    default boolean hasAnnotation(String annotationName){
        return ObjectUtils.defaultIfNull(getAllAnnotationName(), Collections.emptyList()).contains(annotationName);
    }

    /**
     * 获取注解信息
     * @param annotationName 注解名称
     * @return 注解信息
     */
    @SuppressWarnings("unchecked")
    default <T extends Annotation> T getAnnotation(Class<T> annotationName){
        return (T) getAnnotation(annotationName.getName());
    }

}
