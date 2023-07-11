package us.codecraft.webmagic.annotations.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.function.impl.StringToAny;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.PageResult;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.annotations.Extract;
import us.codecraft.webmagic.annotations.ExtractUrl;
import us.codecraft.webmagic.annotations.metadata.ClassMetadata;
import us.codecraft.webmagic.annotations.metadata.FieldMetadata;
import us.codecraft.webmagic.emus.ExtractTypeEnum;
import us.codecraft.webmagic.selector.Selectable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 处理字段的解析信息
 * @author liu xw
 * @date 2023 07-06
 */
public class ModelExtractParser implements ExtractParser<ClassMetadata> {

    @Override
    public Object parser(ClassMetadata metadata, Page page) {
        PageResult result = new PageResult();

        // 解析类属性信息
        List<ExtractUrl> extractUrlList = new ArrayList<>();


        // 1. 解析类上的注解信息
        if (metadata.hasAnnotation(ExtractUrl.class.getName())) {
            // TODO 因为抓的URL对应的不可能是一个实体类. 所以配置了多个
            extractUrlList.add(metadata.getAnnotation(ExtractUrl.class));
        }


        // 1.1 解析 ExtractUrl 注解信息 转化为新增请求
        List<Request> requests = extractUrlList.stream()
            .map(extractUrl -> parserOnExtractUrl(extractUrl, page))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        result.setHatchReqs(requests);

        // 2. 解析字段信息
        Object metadataContextObject = null;
        for (FieldMetadata fieldMetadata : metadata.metadataOnField()) {
            if (!fieldMetadata.hasAnnotation(Extract.class.getName())) {
                continue;
            }
            if (metadataContextObject == null){
                metadataContextObject = metadata.getContextObject();
            }
            Object fieldValue = convertParserField(fieldMetadata, parserOnExtract(fieldMetadata.getAnnotation(Extract.class), page));
            if (fieldValue != null){
                if (fieldValue instanceof String){
                    // 去除空格
                    fieldValue = ((String) fieldValue).trim();
                }
                fieldMetadata.getField().setAccessible(Boolean.TRUE);
                try {
                    fieldMetadata.getField().set(metadataContextObject, fieldValue);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        result.setExtract(Objects.isNull(metadataContextObject) ? null : JSON.toJSONString(metadataContextObject));

        return result;
    }

    /**
     * 解析注解 {@link ExtractUrl} 信息. 获取到需要抓取的请求头数据信息
     * @param extractUrl 注解信息
     * @param page 页面信息
     * @return 结果
     */
    private Collection<Request> parserOnExtractUrl(ExtractUrl extractUrl, Page page){
        // 1. 把 extractUrl 注解组合 extract 注解进行数据解析。 拿到需要抓取的URL地址信息
        List<String> requestStrList = parserOnExtract(extractUrl.extract(), page);

        // 1.5 补充判断正则过滤器
        if (StringUtils.isNotBlank(extractUrl.filterRegEx())){
            requestStrList = requestStrList.stream()
                    .filter(StringUtils::isNotBlank)
                    .filter(req -> Pattern.matches (extractUrl.filterRegEx(),req))
                    .collect(Collectors.toList());
        }

        // 2. 把拿到的 request 请求地址信息转化为 Request 对象
        List<Request> requests = new ArrayList<>();
        for (String requestStr : requestStrList) {
            requests.add(new Request(requestStr, extractUrl.modelClass()));
        }
        return requests;
    }

    /**
     * 解析 {@link Extract} 注解信息
     * @param extract 解析注解
     * @param page 页面信息
     * @return 结果
     */
    private List<String> parserOnExtract(Extract extract, Page page) {
        if (extract == null){
            return null;
        }
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

        return originalDataList;
    }




    /**
     * 选择需要解析的领域数据 - 属性
     * @param fieldMetadata 字段信息
     * @param originalDataList 原始数据信息
     * @return 解析后的结果数据
     */
    private Object convertParserField(FieldMetadata fieldMetadata, List<String> originalDataList){
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
