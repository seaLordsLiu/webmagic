package us.codecraft.webmagic.processor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.PageResult;
import us.codecraft.webmagic.annotations.component.ModelExtractParser;
import us.codecraft.webmagic.annotations.metadata.MetadataContext;
import us.codecraft.webmagic.annotations.MetadataContextFactory;

/**
 * 界面提取解析管理中心
 * @author liu xw
 * @date 2023 07-07
 */
public class AnnotationsPageProcessor implements PageProcessor{

    /**
     * 元数据中心工厂
     */
    private final MetadataContextFactory metadataContextFactory;

    /**
     * 解析字段的
     */
    private final ModelExtractParser fieldExtractParser;

    public AnnotationsPageProcessor() {
        this.metadataContextFactory = new MetadataContextFactory();
        this.fieldExtractParser = new ModelExtractParser();
    }

    @Override
    public void process(PageResult result, Page page) {
        // 1. 获取到类元数据信息
        MetadataContext context = metadataContextFactory.getMetadataContext(page.getRequest().getExtractClazz());

        // 映射结果集
        fieldExtractParser.parser(context.metadataOnClass(), page, result);
    }
}