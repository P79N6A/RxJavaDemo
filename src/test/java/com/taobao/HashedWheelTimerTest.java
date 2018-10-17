package com.taobao;

import io.netty.util.HashedWheelTimer;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/15 下午5:37
 */
public class HashedWheelTimerTest {

    @Test
    public void test() throws InterruptedException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        HashedWheelTimer timer = new HashedWheelTimer(1, TimeUnit.SECONDS, 60);
        System.out.println("start:" + LocalDateTime.now().format(formatter));

        timer.newTimeout(timeout -> System.out.println("task :" + LocalDateTime.now().format(formatter)), 3, TimeUnit.SECONDS);

        //当前一个任务执行时间过长的时候，会影响后续任务的到期执行时间的
        timer.newTimeout(timeout -> {
            TimeUnit.SECONDS.sleep(3);
            System.out.println("task1 :" + LocalDateTime.now().format(formatter));
        }, 3, TimeUnit.SECONDS);

        timer.newTimeout(timeout -> System.out.println("task2 :" + LocalDateTime.now().format(formatter)), 3, TimeUnit.SECONDS);

        TimeUnit.SECONDS.sleep(1000);
    }
}
