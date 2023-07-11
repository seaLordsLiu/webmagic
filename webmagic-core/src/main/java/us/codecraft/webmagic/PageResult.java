package us.codecraft.webmagic;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 界面解析结果
 * @author liu xw
 * @date 2023 07-03
 */
@Data
@RequiredArgsConstructor
public class PageResult {

    /**
     * 是否执行管道计划
     * TRUE: 执行
     * FALSE: 不执行
     */
    private Boolean skip = Boolean.TRUE;

    /**
     * 需要新添加的请求（新孵化请求）
     */
    private List<Request> hatchReqs;

    /**
     * 获取结果信息
     * 使用Json保存进行传输
     */
    private String extract;

    /**
     * 数据结果集的Class类
     */
    private Class<?> extractClass;
}
