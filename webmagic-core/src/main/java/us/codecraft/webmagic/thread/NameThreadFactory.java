package us.codecraft.webmagic.thread;

import lombok.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 指定名称的线程工厂
 * @author liu xw
 * @date 2023 06-29
 */
public class NameThreadFactory implements ThreadFactory {

    /**
     * 名称
     */
    private final String threadName;

    /**
     * 计数
     */
    private final AtomicInteger threadNum = new AtomicInteger(1);

    public NameThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Thread thread = new Thread(r, threadName + "-" + threadNum.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }
}
