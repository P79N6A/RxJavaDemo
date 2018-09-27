package com.taobao.blocking;

import io.reactivex.Observable;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/12 上午6:32
 */
public class BlockingObservableTest {

    Observable<Long> observable;

    @Before
    public void before() {
        observable = Observable.interval(0, 1, TimeUnit.SECONDS);
    }

    @Test
    public void forEach() {
        observable.blockingForEach(System.out::println);
    }

    @Test
    public void first() {
        System.out.println(observable.blockingFirst());
    }

    @Test
    public void last() {
        Observable<String> observable = Observable.just("A", "B");
        System.out.println(observable.blockingLast());
    }

    @Test
    public void mostRecent() throws InterruptedException {
        Observable.just(1L, 2L, 3L, 4L).blockingMostRecent(1L).forEach(System.out::println);
        TimeUnit.DAYS.sleep(1);
    }

    @Test
    public void next() {
        observable.blockingNext().forEach(System.out::println);

    }

    @Test
    public void latest() {
        observable.blockingLatest().forEach(System.out::println);
        //Observable.just("A", "B").blockingLatest().forEach(System.out::println);
    }

    @Test
    public void single() {
        //System.out.println(Observable.just("A").blockingSingle());
        System.out.println(Observable.just("A", "B").blockingSingle("A"));
    }
}
