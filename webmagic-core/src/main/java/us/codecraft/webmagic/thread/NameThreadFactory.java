package us.codecraft.webmagic.thread;

import lombok.NonNull;

import java.util.concurrent.ThreadFactory;

/**
 * 指定名称的线程工厂
 * @author liu xw
 * @date 2023 06-29
 */
public class NameThreadFactory implements ThreadFactory {

    private final String threadName;

    public NameThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Thread thread = new Thread(r, threadName);
        thread.setDaemon(true);
        return thread;
    }
}
