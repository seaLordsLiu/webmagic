package us.codecraft.webmagic.annotations;

import us.codecraft.webmagic.annotations.metadata.ClassMetadata;
import us.codecraft.webmagic.annotations.metadata.MetadataContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 元数据上下文工厂
 * @author liu xw
 * @date 2023 07-07
 */
public class MetadataContextFactory {

    /**
     * 缓存数据信息
     */
    private final Map<String, MetadataContext> metadataContextCache = new ConcurrentHashMap<>();

    /**
     * 获取信息
     */
    public MetadataContext getMetadataContext(Class<?> clazz) {
        if (metadataContextCache.containsKey(clazz.getName())) {
            return metadataContextCache.get(clazz.getName());
        }
        ClassMetadata classMetadata = new ClassMetadata(clazz);
        metadataContextCache.put(clazz.getName(), classMetadata);
        return classMetadata;
    }

}
