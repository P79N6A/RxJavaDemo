package com.taobao.combining;

import io.reactivex.Observable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * When an item is emitted by either of two Observables,
 * combine the latest item emitted by each Observable via a specified function and emit items based on the results of this function.
 *
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/11 上午6:37
 */
public class CombineLatestTest {

    @Test
    public void test() {

        Observable<Long> ob1 = Observable.interval(0, 3, TimeUnit.SECONDS);
        Observable<Long> ob2 = Observable.interval(0, 1, TimeUnit.SECONDS);

        Observable.combineLatest(ob1, ob2, (left, right) -> "left:" + left + ", right:" + right).blockingSubscribe(System.out::println);
    }

    @Test
    public void test1() {
        Observable<Long> ob1 = Observable.just(1L,2L);
        Observable<Long> ob2 = Observable.just(3L,4L);

        Observable.combineLatest(ob1, ob2, (left, right) -> "left:" + left + ", right:" + right).blockingSubscribe(System.out::println);
    }
}
