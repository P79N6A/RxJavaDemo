package com.taobao.time;

import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/13 上午9:11
 */
public class TimingWheelTest {

    @Test
    public void testStart() throws InterruptedException {

        TimingWheel<Integer> timingWheel = new TimingWheel(1, 2, TimeUnit.MILLISECONDS);
        timingWheel.addExpirationListener(i -> System.out.println("接收处理事件：" + i));
        timingWheel.start();
        System.out.println(timingWheel.add(1));
        System.out.println(timingWheel.add(2));

        TimeUnit.HOURS.sleep(3);
        timingWheel.stop();
    }
}