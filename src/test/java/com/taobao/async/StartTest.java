package com.taobao.async;

import io.reactivex.Observable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * create an Observable that emits the return value of a function-like directive
 *
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/14 上午6:10
 */
public class StartTest {

    @Test
    public void test() {

        Observable.fromCallable(() -> {
            System.out.println("wait....");
            TimeUnit.SECONDS.sleep(3);
            return "complete";
        }).doFinally(() -> System.out.println("finish")).blockingSubscribe(System.out::println);


    }
}
