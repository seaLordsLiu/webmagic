package us.codecraft.webmagic.scheduler.component;

import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.Scheduler;

/**
 * 监控调度程序
 * @author liu xw
 * @date 2023 05-12
 */
public interface MonitorableScheduler extends Scheduler {

    /**
     * 判断是否为空
     * @return ture是. false不是
     */
    boolean isEmpty(Task task);

    /**
     * 执行了的请求数量
     */
    int totalCount(Task task);

    /**
     * 待执行的请求数量
     */
    int leftCount(Task task);
}
