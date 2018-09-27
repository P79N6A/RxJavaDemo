package com.taobao.conditional;

import io.reactivex.Observable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/14 上午6:26
 */
public class ConditionalTest {

    @Test
    public void DefaultIfEmpty() {
        Observable<Long> observable = Observable.empty();
        observable.defaultIfEmpty(0L).blockingSubscribe(System.out::println);
    }

    @Test
    public void repeat() {
        Observable observable = Observable.intervalRange(1, 3, 0, 1, TimeUnit.SECONDS);
        observable.repeat(3).subscribe(System.out::println);

    }

    @Test
    public void repeatUntil() {
        Observable observable = Observable.just(1, 2, 3);
        //重复三秒
        long current = System.currentTimeMillis();
        observable.repeatUntil(() -> System.currentTimeMillis() > (current + 3000)).subscribe(System.out::println);
    }

    @Test
    public void repeatWhen() {
        Observable observable = Observable.just(1, 2, 3);
        observable.repeatWhen(objectObservable -> {
            return Observable.just(1);
        }).subscribe(System.out::println);
    }

    @Test
    public void skip() {
        Observable observable = Observable.just(1, 2, 3);
        observable.skip(1).subscribe(System.out::println);
    }

    @Test
    public void skipTime() {
        Observable observable = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS);
        observable.skip(1, TimeUnit.SECONDS).blockingSubscribe(System.out::println);
    }

    @Test
    public void skipLast() {
        Observable observable = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS);
        observable.skipLast(3).blockingSubscribe(System.out::println);
    }

    @Test
    public void skipUntil() {
        Observable observable = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS);
        observable.skipUntil(Observable.timer(3, TimeUnit.SECONDS)).blockingSubscribe(System.out::println);
    }

    @Test
    public void skipWhile() {
        Observable<Long> observable = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS);
        observable.skipWhile(number -> {
            System.out.println("receive:" + number);
            return number < 5;
        }).blockingSubscribe(System.out::println);
    }
}
