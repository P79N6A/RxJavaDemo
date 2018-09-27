package com.taobao.scheduler;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/17 下午8:25
 */
public class SchedulerTest {

    @Test
    public void test() {

        System.out.println(String.format("Starting on threadId:%s", Thread.currentThread().getId()));

        Observable source = Observable.create(emitter -> {
            System.out.println(String.format("Invoked on threadId:%s", Thread.currentThread().getId()));
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
            System.out.println(String.format("Finished on threadId:%s", Thread.currentThread().getId()));

        });

        source.subscribeOn(Schedulers.newThread()).subscribe(
                i -> System.out.println(String.format("Received %s on threadId:%s", i, Thread.currentThread().getId())),
                throwable -> System.out.println(throwable),
                () -> System.out.println(String.format("OnCompleted on threadId:%s", Thread.currentThread().getId())));

        System.out.println(String.format("Subscribed on threadId:%s", Thread.currentThread().getId()));

    }
}
