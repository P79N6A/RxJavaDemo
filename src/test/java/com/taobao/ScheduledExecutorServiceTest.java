package com.taobao;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/11/7 上午10:14
 */
public class ScheduledExecutorServiceTest {

    ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);

    @After
    public void after() throws InterruptedException {
        TimeUnit.DAYS.sleep(10);
    }


    @Test
    public void test() throws InterruptedException {
        scheduled.schedule(() -> System.out.println("Run1"), 300, TimeUnit.SECONDS);
        scheduled.schedule(() -> System.out.println("Run1"), 400, TimeUnit.SECONDS);
        scheduled.schedule(() -> System.out.println("Run1"), 500, TimeUnit.SECONDS);
        scheduled.schedule(() -> System.out.println("Run1"), 600, TimeUnit.SECONDS);

    }

    @Test
    public void scheduleAtFixedRate() {
        //If any execution of this task takes longer than its period
        //then subsequent executions may start late, but will not concurrently execute
        scheduled.scheduleAtFixedRate(() -> System.out.println("Run"), 0, 1, TimeUnit.SECONDS);
    }

    @Test
    public void scheduleWithFixedDelay() {
        //Creates and executes a periodic action that becomes enabled first after the given initial delay,
        // and subsequently with the given delay between the termination of one execution and the commencement of the next.
        // If any execution of the task encounters an exception, subsequent executions are suppressed. Otherwise, the task will only terminate via cancellation or termination of the executor.

        scheduled.scheduleWithFixedDelay(() -> System.out.println("Run"), 0, 1, TimeUnit.SECONDS);
    }
}
