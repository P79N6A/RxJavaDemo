package com.taobao.combining;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/5 上午6:51
 */
public class JoinTest {

    @Test
    public void test() throws InterruptedException {

        Observable ob1 = Observable.just(1, 2, 3).delay(100, TimeUnit.MILLISECONDS);

        Observable ob2 = Observable.just(4, 5, 6);

        ob1.join(ob2, new Function<Integer, Observable<String>>() {

            @Override
            public Observable<String> apply(Integer integer) throws Exception {
                System.out.println("left:" + integer);
                return Observable.just(integer.toString()).delay(200, TimeUnit.MILLISECONDS);
            }
        }, new Function<Integer, Observable<String>>() {

            @Override
            public Observable<String> apply(Integer integer) throws Exception {
                System.out.println("right:" + integer);
                return Observable.just(integer.toString()).delay(200, TimeUnit.MILLISECONDS);
            }
        }, new BiFunction<Integer, Integer, String>() {

            @Override
            public String apply(Integer integer, Integer integer2) throws Exception {
                return integer + ":" + integer2;
            }
        }).blockingSubscribe(System.out::println);


    }

    @Test
    public void testJoinGroup() {
        Observable<Long> ob1 = Observable.interval(0, 1, TimeUnit.SECONDS);

        Observable<Long> ob2 = Observable.interval(2, 2, TimeUnit.SECONDS);
        ob1.groupJoin(ob2, aLong -> {
            System.out.println("left接收了：" + aLong);
            return Observable.never();
        }, aLong -> {
            System.out.println("right接收了：" + aLong);
            return Observable.never();
        }, (left, right) -> "left = " + left + ",right = " + right).blockingSubscribe(System.out::println);
    }
}

