package us.codecraft.webmagic;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import us.codecraft.webmagic.annotations.component.format.DefaultExtractFormat;
import us.codecraft.webmagic.annotations.component.format.ExtractFormat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liu xw
 * @date 2023 45-18
 */
@Slf4j
public class ExtractFormatFactory {

    /**
     * 获取解析配置缓存
     */
    private static Map<String, ExtractFormat> EXTRACT_FORMAT_CACHE = new ConcurrentHashMap<>();

    /**
     * 默认的对象信息
     */
    private static final DefaultExtractFormat DEFAULT_EXTRACT_FORMAT = new DefaultExtractFormat();

    {
        EXTRACT_FORMAT_CACHE.put(DefaultExtractFormat.class.getName(), DEFAULT_EXTRACT_FORMAT);
    }

    public static ExtractFormat getExtractFormat(Class<? extends ExtractFormat> extractFormatClass) {
        if (extractFormatClass == null) {
            return DEFAULT_EXTRACT_FORMAT;
        }
        final String className = extractFormatClass.getName();
        if (EXTRACT_FORMAT_CACHE.containsKey(className)) {
            return EXTRACT_FORMAT_CACHE.get(className);
        }
        ExtractFormat extractFormat;
        try {
            extractFormat = ConstructorUtils.invokeConstructor(extractFormatClass);
        } catch (Exception e) {
            log.error("[{}]. 获取解析配置失败", className, e);
            extractFormat = DEFAULT_EXTRACT_FORMAT;
        }
        EXTRACT_FORMAT_CACHE.put(className, extractFormat);
        return extractFormat;
    }

    public static void main(String[] args) {
        System.out.println(ExtractFormatFactory.class.getName());
    }
}
