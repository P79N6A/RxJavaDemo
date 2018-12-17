package com.taobao.scheduler;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.net.URL;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * The subscribeOn()
 * operator is used to suggest to the upstream in an Observable chain which Scheduler to push emissions on. The
 * observeOn()will switch emissions to a different Scheduler at that point in the Observable chain and use that
 * Scheduler downstream. You can use these two operators in conjunction with flatMap() to create powerful
 * parallelization patterns so you can fully utilize your multi-CPU power
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/17 下午8:25
 */
public class SubscribeOnTest {

    @Test
    public void test() {

        System.out.println(String.format("Starting on threadId:%s", Thread.currentThread().getId()));

        Observable source = Observable.create(emitter -> {
            System.out.println(String.format("Invoked on threadId:%s", Thread.currentThread().getId()));
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
            System.out.println(String.format("Finished on threadId:%s", Thread.currentThread().getId()));

        });

        source.subscribeOn(Schedulers.newThread()).subscribe(
                i -> System.out.println(String.format("Received %s on threadId:%s", i, Thread.currentThread().getId())),
                throwable -> System.out.println(throwable),
                () -> System.out.println(String.format("OnCompleted on threadId:%s", Thread.currentThread().getId())));

        System.out.println(String.format("Subscribed on threadId:%s", Thread.currentThread().getId()));

    }

    @Test
    public void test1() {
        //All three accomplish the same effect with subscribeOn()
        Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .subscribeOn(Schedulers.computation()) //preferred
                .map(String::length)
                .filter(i -> i > 5)
                .subscribe(System.out::println);
        Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .map(String::length)
                .subscribeOn(Schedulers.computation())
                .filter(i -> i > 5)
                .subscribe(System.out::println);
        Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .map(String::length)
                .filter(i -> i > 5)
                .subscribeOn(Schedulers.computation())
                .subscribe(System.out::println);
    }

    @Test
    public void test2() {
        //Having multiple Observers to the same Observable with subscribeOn() will result in each one getting its own
        //thread (or have them waiting for an available thread if none are available)
        Observable<Integer> lengths =
                Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                        .subscribeOn(Schedulers.computation())
                        .map(s -> intenseCalculation(s))
                        .map(String::length);
        lengths.subscribe(i ->
                System.out.println("Received " + i + " on thread " +
                        Thread.currentThread().getName()));
        lengths.subscribe(i ->
                System.out.println("Received " + i + " on thread " +
                        Thread.currentThread().getName()));
        sleep(10000);
    }

    @Test
    public void test3() {
        //if we want only one thread to serve both Observers, we can
        //multicast this operation. Just make sure subscribeOn() is before the multicast operators
        Observable<Integer> lengths =
                Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                        .subscribeOn(Schedulers.computation())
                        .map(s -> intenseCalculation(s))
                        .map(String::length)
                        .publish()
                        .autoConnect(2);
        lengths.subscribe(i ->
                System.out.println("1 Received " + i + " on thread " +
                        Thread.currentThread().getName()));
        lengths.subscribe(i ->
                System.out.println("2 Received " + i + " on thread " +
                        Thread.currentThread().getName()));
        sleep(10000);
    }

    @Test
    public void test4() {
        Observable.fromCallable(() ->
                getResponse("https://api.github.com/users/thomasnield/starred")
        ).subscribeOn(Schedulers.io())
                .subscribe(System.out::println);
        sleep(10000);
    }

    @Test
    public void test5() {
        //if you have multiple subscribeOn() calls on a given Observable chain, the
        //top-most one, or the one closest to the source, will win and cause any subsequent ones to have no
        //practical effect (other than unnecessary resource usage). If I call subscribeOn() with Schedulers.computation()
        //and then call subscribeOn() for Schedulers.io(), Schedulers.computation() is the one that will be used
        Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .subscribeOn(Schedulers.computation())
                .filter(s -> s.length() == 5)
                .subscribeOn(Schedulers.io())
                .subscribe(i -> System.out.println("Received " + i +
                        " on thread " + Thread.currentThread().getName()));
        sleep(5000);
    }

    @Test
    public void test6() {
        Observable.range(1, 10)
                .map(i -> intenseCalculation(i))
                .subscribe(i -> System.out.println("Received " + i + " " + LocalTime.now()));

    }

    @Test
    public void test7() {
        //The example here is not necessarily optimal, however. Creating an Observable for each emission might
        //create some unwanted overhead. There is a leaner way to achieve parallelization, although it has a few
        //more moving parts. If we want to avoid creating excessive Observable instances, maybe we should split the
        //source Observable into a fixed number of Observables where emissions are evenly divided and distributed
        //through each one. Then, we can parallelize and merge them with flatMap().
        Observable.range(1, 10)
                .flatMap(i -> Observable.just(i)
                        .subscribeOn(Schedulers.computation())
                        .map(i2 -> intenseCalculation(i2))
                )
                .subscribe(i -> System.out.println("Received " + i + " "
                        + LocalTime.now() + " on thread "
                        + Thread.currentThread().getName()));
        sleep(20000);
    }

    @Test
    public void test8() {
        //We can achieve this using a groupBy() trick. If I have eight cores, I want to key each emission to a number in
        //the range 0 through 7. This will yield me eight GroupedObservables that cleanly divide the emissions into eight
        //streams. More specifically, I want to cycle through these eight numbers and assign them as a key to each
        //emission. GroupedObservables are not necessarily impacted by subscribeOn() (it will emit on the source's thread
        //with the exception of the cached emissions), so I will need to use observeOn() to parallelize them instead. I
        //can also use an io() or newThread() scheduler since I have already constrained the number of workers to the
        //number of cores, simply by constraining the number of GroupedObservables
        //核心数
        int coreCount = Runtime.getRuntime().availableProcessors();
        System.out.println("core count:" + coreCount);
        AtomicInteger assigner = new AtomicInteger(0);
        Observable.range(1, 10)
                .groupBy(i -> assigner.incrementAndGet() % coreCount)
                .flatMap(grp -> grp.observeOn(Schedulers.io())
                        .map(i2 -> intenseCalculation(i2))
                )
                .subscribe(i -> System.out.println("Received " + i + " "
                        + LocalTime.now() + " on thread "
                        + Thread.currentThread().getName()));
        sleep(20000);
    }

    @Test
    public void test9() {
        Disposable d = Observable.interval(1, TimeUnit.SECONDS)
                .doOnDispose(() -> System.out.println("Disposing on thread "
                        + Thread.currentThread().getName()))
                .subscribe(i -> System.out.println("Received " + i));
        sleep(3000);
        d.dispose();
        sleep(3000);
    }

    @Test
    public void unsubscribeOn() {
        //When you dispose an Observable,
        //sometimes, that can be an expensive operation depending on the nature of the source. For instance, if your
        //Observable is emitting the results of a database query using RxJava-JDBC, (https://github.com/davidmoten/rxjava-jdbc)
        //it can be expensive to stop and dispose that Observable because it needs to shut down the JDBC resources it
        //is using.
        Disposable d = Observable.interval(1, TimeUnit.SECONDS)
                .doOnDispose(() -> System.out.println("Disposing on thread "
                        + Thread.currentThread().getName()))
                .unsubscribeOn(Schedulers.io())
                .subscribe(i -> System.out.println("Received " + i));
        sleep(3000);
        d.dispose();
        sleep(3000);
    }


    private static String getResponse(String path) {
        try {
            return new Scanner(new URL(path).openStream(), "UTF-8").useDelimiter("\\A").next();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static <T> T intenseCalculation(T value) {
        sleep(ThreadLocalRandom.current().nextInt(3000));
        return value;
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
