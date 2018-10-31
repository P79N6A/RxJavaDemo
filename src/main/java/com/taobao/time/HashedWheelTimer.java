package com.taobao.time;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 采用国定启动时间的定时轮，支持分布式部署
 * <p>
 * 当Timer启动过后会根据启动时间计算出 当前处理的Tick，当前的Index，以及走了多少圈了
 *
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/26 上午10:17
 */
public class HashedWheelTimer {


    /**
     * wheel的长度
     */
    private static final int WHEEL_LENGTH = 1 << 10;

    /**
     * 用于计算index=mask & number
     */
    private static final int mask = WHEEL_LENGTH - 1;

    /**
     * 默认启动时间
     */
    private static final Long START_TIME = 363765872563588L;

    /**
     * 时间轮
     */
    private final WheelBucket[] wheel = new WheelBucket[WHEEL_LENGTH];

    /**
     * 时间间隔
     */
    private final long tickDuration;

    private long tick;

    Thread thread;


    public HashedWheelTimer(long tickDuration, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        if (tickDuration <= 0) {
            throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration);
        }

        this.tickDuration = unit.toNanos(tickDuration);

        //初始化wheel
        IntStream.of(WHEEL_LENGTH - 1).forEach(i -> wheel[i] = new WheelBucket());

        thread = new Thread(new Worker());
        thread.setDaemon(true);
        thread.start();

    }


    private class Worker implements Runnable {

        @Override
        public void run() {

            tick = (System.nanoTime() - START_TIME) / tickDuration;

            while (true) {
                long deadline = waitForNextTick();
                if (deadline > 0) {
                    int idx = (int) (tick & mask);
                    WheelBucket bucket = wheel[idx];
                    System.out.println("tick:" + tick + " index:" + idx);
                    tick++;
                }
                //加载任务
                //执行任务
            }
        }
    }

    /**
     * 等待一个tick
     *
     * @return
     */
    private long waitForNextTick() {
        long deadline = tickDuration * (tick + 1);
        while (true) {
            long currentTime = System.nanoTime() - START_TIME;
            long sleepTime = (deadline - currentTime + 999999) / 1000000;
            if (sleepTime <= 0) {
                //防止益处
                if (currentTime == Long.MIN_VALUE) {
                    return -Long.MAX_VALUE;
                } else {
                    return currentTime;
                }
            }

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                return -Long.MAX_VALUE;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        HashedWheelTimer timer = new HashedWheelTimer(3, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(10);
    }
}
