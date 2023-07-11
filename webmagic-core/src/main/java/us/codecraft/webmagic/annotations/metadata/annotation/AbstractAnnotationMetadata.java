package us.codecraft.webmagic.annotations.metadata.annotation;


import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象处理的注解方法信息
 * @author liu xw
 * @date 2023 07-07
 */
public abstract class AbstractAnnotationMetadata implements AnnotationMetadata {

    /**
     * 注解信息缓存
     */
    @Getter
    private final Map<String, Annotation> annotationCache = new ConcurrentHashMap<>();

    @Override
    public Set<String> getAllAnnotationName() {
        return annotationCache.keySet();
    }

    @Override
    public boolean hasAnnotation(String annotationName) {
        if (!annotationCache.containsKey(annotationName)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Annotation getAnnotation(String annotationName) {
        if (hasAnnotation(annotationName)) {
            return annotationCache.get(annotationName);
        }
        throw new AnnotationTypeMismatchException(null ,annotationName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> annotationName) {
        return (T) getAnnotation(annotationName.getName());
    }

}