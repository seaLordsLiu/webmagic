package us.codecraft.webmagic.scheduler;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

/**
 * 当前端指定数据采集时，使用该调度器
 * 因为前端提供的 URL 会衍生出一个或者多个 request 并且在并发场景下，这些 request 又需要进行隔离
 * 所以使用 TheadLocal 进行隔离不同线程的消息队列
 * @author liu xw
 * @date 2023 07-03
 */
public class ThreadLocalQueueScheduler extends DuplicateRemovedScheduler {

    private static final ThreadLocal<QueueScheduler> threadLocalContextQueue = new InheritableThreadLocal<>();

    /**
     * 队列最大长度信息
     */
    private static final Integer MAX_QUEUE_SIZE = 100;

    public static void clearContext(){
        threadLocalContextQueue.remove();
    }

    @Override
    public DuplicateRemover getDuplicateRemover() {
        return this.getContext().getDuplicateRemover();
    }

    @Override
    public DuplicateRemovedScheduler setDuplicateRemover(DuplicateRemover duplicatedRemover) {
        return this.getContext().setDuplicateRemover(duplicatedRemover);
    }

    @Override
    protected boolean shouldReserved(Request request) {
        return this.getContext().shouldReserved(request);
    }

    @Override
    protected boolean noNeedToRemoveDuplicate(Request request) {
        return this.getContext().noNeedToRemoveDuplicate(request);
    }

    @Override
    public void push(Request request, Task task) {
        this.getContext().push(request,task);
    }

    @Override
    protected void pushWhenNoDuplicate(Request request, Task task) {
        this.getContext().pushWhenNoDuplicate(request,task);
    }

    @Override
    public Request poll(Task task) {
        return this.getContext().poll(task);
    }

    public QueueScheduler getContext() {
        QueueScheduler ctx = threadLocalContextQueue.get();

        if (ctx == null) {
            ctx = createEmptyContext();
            threadLocalContextQueue.set(ctx);
        }

        return ctx;
    }

    private QueueScheduler createEmptyContext(){
        return new QueueScheduler(MAX_QUEUE_SIZE);
    }
}
