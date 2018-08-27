package com.taobao;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Action;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/8/28 上午6:41
 */
public class FromTest {

    @Test
    public void fromIterable() {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        Observable<Integer> observable = Observable.fromIterable(list);
        observable.subscribe(i -> System.out.println(i));

    }

    @Test
    public void fromArray() {
        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7};
        Observable<Integer> observable = Observable.fromArray(array);
        observable.subscribe(i -> System.out.println(i));

    }

    @Test
    public void fromCallable() {

        Callable<Integer> callable = () -> {
            Random random = new Random();
            return random.nextInt();
        };

        Observable<Integer> observable = Observable.fromCallable(callable);
        observable.subscribe(
                item -> System.out.println(item),
                error -> error.printStackTrace(),
                () -> System.out.println("Done")
        );
    }

    @Test
    public void fromAction() {
        Action action = () -> System.out.println("Hello World!");
        Completable completable = Completable.fromAction(action);
        completable.subscribe(() -> System.out.println("Done"), error -> error.printStackTrace());

    }

    @Test
    public void fromRunnable() {
        Runnable runnable = () -> System.out.println("Hello World!");
        Completable completable = Completable.fromRunnable(runnable);
        completable.subscribe(() -> System.out.println("Done"), error -> error.getMessage());
    }

    @Test
    public void fromFuture() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        Future<Integer> future = executor.schedule(() -> {
            Random random = new Random();
            return random.nextInt();
        }, 10, TimeUnit.SECONDS);

        Observable<Integer> observable = Observable.fromFuture(future);

        observable.subscribe(
                item -> System.out.println(item),
                error -> error.printStackTrace(),
                () -> System.out.println("Done"));

        executor.shutdown();
    }

    @Test
    public void fromPublisher() {

        Observable<Integer> observable = Observable.just(1, 2, 3);

        Completable completable = Completable.fromObservable(observable);

        completable.subscribe(()->System.out.println("Done"));
    }
}
