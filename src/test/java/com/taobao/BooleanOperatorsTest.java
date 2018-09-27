package com.taobao;

import io.reactivex.Observable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/25 下午8:24
 */
public class BooleanOperatorsTest {

    @Test
    public void all() {
        Observable.just(1, 2, 3, 4).all(i -> i < 10).subscribe(System.out::println);
    }

    @Test
    public void contains() {
        boolean contains = Observable.intervalRange(0, 10, 0, 1, TimeUnit.MILLISECONDS).contains(0L).blockingGet();
        System.out.println(contains);
    }

    @Test
    public void empty() {
        Observable.just(1, 2, 3).isEmpty().subscribe(System.out::println);
    }

    @Test
    public void amb() {
        Observable.ambArray(Observable.intervalRange(0, 9, 1, 1, TimeUnit.SECONDS)
                , Observable.intervalRange(10, 20, 0, 1, TimeUnit.SECONDS))
                .blockingSubscribe(System.out::println);
    }

    @Test
    public void sequenceEqual() {
        Observable.sequenceEqual(Observable.just(1, 2, 3), Observable.just(1, 2, 3)).subscribe(System.out::println);
    }
}
