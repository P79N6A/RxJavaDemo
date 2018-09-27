package com.taobao.combining;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/11 上午7:48
 */
public class SwitchTest {

    @Test
    public void test() {

        Observable<Observable<String>> timeIntervals =
                Observable.interval(1, TimeUnit.SECONDS)
                        .map(ticks -> Observable.interval(100, TimeUnit.MILLISECONDS)
                                .map(innerInterval -> "outer: " + ticks + " - inner: " + innerInterval));

        Observable.switchOnNext(timeIntervals)
                .blockingSubscribe(item -> System.out.println(item));
    }
}
