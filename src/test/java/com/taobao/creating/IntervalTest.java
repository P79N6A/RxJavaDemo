package com.taobao.creating;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/8/30 上午6:36
 */
public class IntervalTest {

    @Test
    public void test() throws InterruptedException {
        //可以无限产生数字
        Observable<Long> observable = Observable.interval(1, TimeUnit.SECONDS);
        observable.subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("定时触发了，开始干活了...." + aLong);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        TimeUnit.SECONDS.sleep(20);
    }

    @Test
    public void scheduler() throws InterruptedException {
        Observable<Long> observable = Observable.interval(1, TimeUnit.SECONDS, new Scheduler() {
            @Override
            public Worker createWorker() {
                return new Worker() {
                    @Override
                    public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
                        System.out.println("schedule");
                        return this;
                    }

                    @Override
                    public void dispose() {

                    }

                    @Override
                    public boolean isDisposed() {
                        return false;
                    }
                };
            }
        });

        observable.subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(20);
    }

    @Test
    public void intervalRange() throws InterruptedException {
        Observable<Long> observable = Observable.intervalRange(1, 10, 0, 1, TimeUnit.SECONDS);
        observable.subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(20);
    }
}
