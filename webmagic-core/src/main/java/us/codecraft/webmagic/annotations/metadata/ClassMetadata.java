package us.codecraft.webmagic.annotations.metadata;

import org.apache.commons.lang3.ArrayUtils;
import us.codecraft.webmagic.annotations.metadata.annotation.AbstractAnnotationMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 模块的解析上下文信息
 * 1. 指定model类的相关处理信息
 * 2. 指定model中field信息的处理信息
 * @see us.codecraft.webmagic.annotations.metadata.FieldMetadata
 * @author liu xw
 * @date 2023 07-07
 */
public class ClassMetadata extends AbstractAnnotationMetadata implements MetadataContext {


    public ClassMetadata(Class<?> modelClass) {
        this.modelClass = modelClass;
        // 添加类的注解信息
        Annotation[] annotations = modelClass.getAnnotations();
        if (ArrayUtils.isNotEmpty(annotations)){
            for (Annotation annotation : annotations) {
                this.getAnnotationCache().put(annotation.annotationType().getName(), annotation);
            }
        }


        List<FieldMetadata> fieldMetadataCache = new ArrayList<>();
        // 补充字段信息
        for (Field field : getContextClass().getDeclaredFields()) {
            fieldMetadataCache.add(new FieldMetadata(field));
        }
        // 不允许修改
        this.fieldMetadataList = Collections.unmodifiableList(fieldMetadataCache);
    }

    /**
     * model对应的类信息
     */
    private final Class<?> modelClass;

    /**
     * 类字段的元数据信息
     */
    private final List<FieldMetadata> fieldMetadataList;


    @Override
    public Class<?> getContextClass() {
        return this.modelClass;
    }

    @Override
    public Object getContextObject() {
        for (Constructor<?> constructor : this.modelClass.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                // 执行无参构造
                try {
                    return constructor.newInstance();
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }
        throw new InvalidParameterException("无法实例化对象");
    }

    @Override
    public List<FieldMetadata> metadataOnField() {
        return this.fieldMetadataList;
    }

    @Override
    public ClassMetadata metadataOnClass() {
        return this;
    }
}