package com.taobao.scheduler;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/17 下午8:25
 */
public class SchedulerTest {

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
