package us.codecraft.webmagic.scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import us.codecraft.webmagic.Request;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程变量队列测试
 * @author liu xw
 * @date 2023 07-03
 */
public class ThreadLocalQueueSchedulerTest {

    private ThreadLocalQueueScheduler scheduler;

    @Before
    public void before() {
        scheduler = new ThreadLocalQueueScheduler();
    }

    /**
     * 验证并发场景下每个线程中的队列是否进行隔离
     *
     */
    @SneakyThrows
    @Test
    public void testThread() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++){
            int finalI = i;
            executorService.execute(() -> {
                Thread.currentThread().setName("thread-" + finalI);

                int size = new Random().nextInt(20) + 1;
                for (int j = 1; j <= size; j++){
//                    scheduler.push(new Request("" + j),null);
                }

                System.out.println((Thread.currentThread().getName() + ". 期望 -> {"+size+"}. 实际数值 -> {"+scheduler.getContext()+"}"));
            });
        }

        Thread.sleep(10000000000L);
    }


    public static void main(String[] args) {
        System.out.println(new Random().nextInt());
    }
}