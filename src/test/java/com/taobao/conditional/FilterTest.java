package com.taobao.conditional;

import io.reactivex.Observable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/17 上午7:42
 */
public class FilterTest {

    @Test
    public void test() {
        Observable<Long> observable = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS);
        observable.filter(number -> number > 5).blockingSubscribe(System.out::println);
    }
}
