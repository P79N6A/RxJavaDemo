package com.taobao.conditional;

import io.reactivex.Observable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/17 上午6:58
 */
public class TakeTest {

    @Test
    public void take() {
        Observable observable = Observable.just(1, 2, 3, 4, 5);
        observable.take(2).subscribe(System.out::println);
    }

    @Test
    public void takeWithTime() {
        Observable observable = Observable.interval(0, 1, TimeUnit.SECONDS);
        observable.take(1, TimeUnit.SECONDS).subscribe(System.out::println);
    }

    @Test
    public void takeLast() {
        Observable observable = Observable.just(1, 2, 3, 4, 5);
        observable.takeLast(2).subscribe(System.out::println);
    }

    @Test
    public void takeUntil() {
        Observable observable = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS);
        observable.takeUntil(Observable.timer(3, TimeUnit.SECONDS)).blockingSubscribe(System.out::println);
    }
}
