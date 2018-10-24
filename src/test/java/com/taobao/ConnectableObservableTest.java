package com.taobao;

import io.reactivex.Observable;
import io.reactivex.observables.ConnectableObservable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/23 上午9:46
 */
public class ConnectableObservableTest {

    @Test
    public void test() throws InterruptedException {
        ConnectableObservable<Long> source = Observable.interval(1, TimeUnit.SECONDS).publish();

        //第一个订阅
        source.subscribe(s -> System.out.println("Observer 1:" + s), throwable -> throwable.printStackTrace(), () -> System.out.println("Observer 1 complete"));

        //Fire!
        source.connect();

        TimeUnit.SECONDS.sleep(3);

        //第二个订阅
        source.subscribe(s -> System.out.println("Observer 2:" + s), throwable -> throwable.printStackTrace(), () -> System.out.println("Observer 3 complete"));

        TimeUnit.SECONDS.sleep(5);
    }
}
