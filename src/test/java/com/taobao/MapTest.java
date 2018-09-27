package com.taobao;

import io.reactivex.Observable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/18 下午7:54
 */
public class MapTest {

    @Test
    public void map() {
        Observable.just(1, 2, 3).map(i -> i * 2).subscribe(System.out::println);
    }

    @Test
    public void flatMap() {
        List<Integer> ls = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        List<Integer> ls1 = Arrays.asList(5, 6, 7, 8, 9, 10, 11);
        Observable.just(ls, ls1).flatMap(iterable -> Observable.fromIterable(iterable)).subscribe(System.out::println);
    }

    @Test
    public void concatMap() {
        System.out.println("==========concatMap prefetch====================");
        Observable.just(1, 2).concatMap(i -> Observable.intervalRange(i, 10, 0, 3, TimeUnit.SECONDS), 10).blockingSubscribe(i -> System.out.println(i + " "));
        System.out.println();
        System.out.println("==========concatMap有序====================");
        Observable.just(1, 2).concatMap(i -> Observable.intervalRange(i, 10, 0, 1, TimeUnit.MILLISECONDS)).blockingSubscribe(i -> System.out.println(i + " "));
        System.out.println();
        System.out.println("==========flatMap乱序====================");
        Observable.just(1, 2).flatMap(i -> Observable.intervalRange(i, 10, 0, 1, TimeUnit.MILLISECONDS)).blockingSubscribe(i -> System.out.println(i + " "));
    }


    @Test
    public void groupBy() {

        Observable.range(1, 10).groupBy(i -> i % 2 == 0 ? "偶数" : "奇数").subscribe(stringIntegerGroupedObservable -> {
            if (stringIntegerGroupedObservable.getKey() == "偶数") {
                stringIntegerGroupedObservable.subscribe(System.out::println);
            }
        });
    }

    @Test
    public void buffer() {
        System.out.println("==========buffer skip====================");
        Observable.intervalRange(1, 10, 0, 1, TimeUnit.MILLISECONDS).buffer(2).blockingSubscribe(ls -> System.out.println("Next:" + ls));
        System.out.println("==========buffer skip====================");
        Observable.intervalRange(1, 10, 0, 1, TimeUnit.MILLISECONDS).buffer(2, 3).blockingSubscribe(ls -> System.out.println("Next:" + ls));
        System.out.println("==========buffer skip bufferSupplier====================");
        Observable.intervalRange(1, 10, 0, 1, TimeUnit.MILLISECONDS).buffer(2, 3, () -> new ArrayList<>()).blockingSubscribe(ls -> System.out.println("Next:" + ls));
        System.out.println("==========buffer timespan timeskip====================");
        Observable.intervalRange(1, 10, 0, 1, TimeUnit.SECONDS).buffer(4, 4, TimeUnit.SECONDS).blockingSubscribe(ls -> System.out.println("Next:" + ls));
    }

    @Test
    public void window() {
        Observable.intervalRange(1, 10, 0, 1, TimeUnit.SECONDS).window(2).blockingSubscribe(ls -> {
            ls.subscribe(System.out::println, throwable -> throwable.printStackTrace(), () -> System.out.println("subset complete"));
        }, throwable -> throwable.printStackTrace(), () -> System.out.println("source complete"));
    }

    @Test
    public void distinct() {
        Observable.just(1, 1, 2, 2, 3, 4).distinct().subscribe(System.out::println);
    }

    @Test
    public void debounce() {
        Observable.interval(0, 2, TimeUnit.SECONDS).debounce(1, TimeUnit.SECONDS).blockingSubscribe(System.out::println);
    }

}
