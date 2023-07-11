package us.codecraft.webmagic.annotations.component;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.function.impl.StringToAny;
import org.apache.commons.collections4.CollectionUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.annotations.Extract;
import us.codecraft.webmagic.annotations.metadata.FieldMetadata;
import us.codecraft.webmagic.emus.ExtractTypeEnum;
import us.codecraft.webmagic.selector.Selectable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 处理字段的解析信息
 * @author liu xw
 * @date 2023 07-06
 */
public class ModelExtractParser implements ExtractParser<FieldMetadata> {

    public void parserAndGive(FieldMetadata fieldMetadata, Page page, Object obj) throws IllegalAccessException {
        Object value = this.parser(fieldMetadata, page);
        if (value != null){
            fieldMetadata.getField().setAccessible(Boolean.TRUE);
            fieldMetadata.getField().set(obj, value);
        }
    }

    @Override
    public Object parser(FieldMetadata fieldMetadata, Page page) {
        if (!fieldMetadata.hasAnnotation(Extract.class.getName())){
            return null;
        }

        // 1. 获取注解信息
        Extract extract = fieldMetadata.getAnnotation(Extract.class);

        // 2. 解析界面信息
        List<String> originalDataList = null;
        if (ExtractTypeEnum.HTML.equals(extract.type())){
            // 2.1 解析HTML类型的数据
            originalDataList = htmlParser(extract, page);
        }
        if (ExtractTypeEnum.JSON.equals(extract.type())){
            // 2.1 解析HTML类型的数据
            originalDataList = jsonParser(extract, page);
        }

        if (CollectionUtils.isEmpty(originalDataList)){
            return null;
        }


        // 3. 对解析后的数据进行处理，填充到目标类中
        return convertParser(fieldMetadata, originalDataList);
    }


    /**
     * 解析HTML类型的数据
     * @param extract 注解信息
     * @param page 页面信息
     */
    private List<String> htmlParser(Extract extract, Page page){
        Selectable selectable = null;
        if (ExtractTypeEnum.HTML.equals(extract.type())){
            // 1. 判断是xpath
            selectable = page.getHtml().xpath(extract.value());
        }
        if (ExtractTypeEnum.JSON.equals(extract.type())){
            // 2. 判断是正则表达式
            Selectable regex = page.getHtml().regex(extract.value());
        }

        if (null == selectable){
            return null;
        }

        // 解析后的原始数据信息
        return selectable.all();
    }


    /**
     * 解析JSON类型的数据
     * @param extract 注解信息
     * @param page 页面信息
     */
    private List<String> jsonParser(Extract extract, Page page){
        // 只支持jsonPath获取对象信息, 统一先使用字符串进行返回
        return page.getJson().jsonPath(extract.value()).all();
    }


    /**
     * 转化器解析器
     * @param fieldMetadata 字段信息
     * @param originalDataList 原始数据信息
     * @return 解析后的结果数据
     */
    private Object convertParser(FieldMetadata fieldMetadata, List<String> originalDataList){
        if (CollectionUtils.isEmpty(originalDataList)){
            return null;
        }

        // 集合处理
        if (Collection.class.isAssignableFrom(fieldMetadata.getField().getType())){
            // 获取集合的泛型类型
            Type genericType = fieldMetadata.getField().getGenericType();

            // 获取集合上的类型 elementType
            if (genericType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericType;
                Type[] typeArguments = paramType.getActualTypeArguments();
                if (typeArguments.length > 0) {
                    Type typeArgument = typeArguments[0];
                    if (typeArgument instanceof Class) {
                        Class<?> elementType = (Class<?>) typeArgument;
                        // 处理集合的泛型类型
                        return convertOnCollection(originalDataList, elementType, fieldMetadata.getField().getType());
                    }
                }
            }
        }

        // 非集合处理
        return convert(originalDataList.get(0), fieldMetadata.getField().getType());
    }

    /**
     * 转化器 - 把数据采集到的字符串转为目标类型
     * @param resultSelectable 采集到的数据信息
     * @param targetClass 目标类型
     */
    @SuppressWarnings("unchecked")
    private  <T> T convert(String resultSelectable, Class<T> targetClass){
        if (targetClass.isPrimitive()){
            // 基本数据类型转化
            return (T) new StringToAny(targetClass, null).apply(resultSelectable);
        }else if (String.class.isAssignableFrom(targetClass)) {
            // 处理字符串类型 - 不进行处理
            return (T) resultSelectable;
        }
        // 处理其他类型 - 使用jsonObject之间进行转化
        return JSONObject.parseObject(resultSelectable, targetClass);
    }

    /**
     * 当使用 {@link Extract} 修饰的参数类型为：{@link Collection} 时 进行的转化方法
     * @param selectableList page解析出来的数据信息
     * @param argumentClass 解析到的集合的范型
     * @param collectionClass 集合的类型
     * @return 解析后到数据集合
     * @param <T> 集合的范型
     */
    private  <T> Collection<T> convertOnCollection(List<String> selectableList, Class<T> argumentClass, Class<?> collectionClass){
        Collection<T> collection = null;
        if (List.class.isAssignableFrom(collectionClass)) {
            // 处理List集合
            collection = new ArrayList<>(selectableList.size());
        }
        if (Set.class.isAssignableFrom(collectionClass)) {
            // 处理Set集合
            collection = new HashSet<>(selectableList.size());
        }

        // 判断collection有效性
        assert collection != null;

        for (String selectable : selectableList) {
            collection.add(convert(selectable, argumentClass));
        }

        return collection;
    }
}
