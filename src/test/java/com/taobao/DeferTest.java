package com.taobao;

import io.reactivex.Observable;
import org.junit.Test;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/8/29 上午6:19
 */
public class DeferTest {

    @Test
    public void test() throws InterruptedException {
        Observable<Long> observable = Observable.defer(() -> {
            long time = System.currentTimeMillis();
            return Observable.just(time);
        });

        observable.subscribe(time -> System.out.println(time));

        Thread.sleep(1000);

        observable.subscribe(time -> System.out.println(time));
    }

    @Test
    public void testSomeType() {
        //It actually prints out "null", since value had yet to be initialized when Observable.just() is called
        SomeType instance = new SomeType();
        Observable<String> observable = instance.valueObservable();
        instance.setValue("Some Value");
        observable.subscribe(System.out::println);
    }

    @Test
    public void testDeferSomeType() {
        SomeType instance = new SomeType();
        Observable<String> observable = instance.deferObservable();
        instance.setValue("Some Value");
        observable.subscribe(System.out::println);
    }

    @Test
    public void testColdObservable() {
        SomeType instance = new SomeType();
        Observable<String> observable = instance.coldObservable();
        instance.setValue("Some Value");
        observable.subscribe(System.out::println);
    }
}
