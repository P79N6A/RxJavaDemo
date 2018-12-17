package com.taobao.scheduler;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/11/29 上午9:52
 */
public class ObserveOnTest {

    @Test
    public void test() {
        //use observeOn() to intercept each emission and push them forward on a different Scheduler. In the
        //preceding example, operators before observeOn() are executed on Scheduler.io(), but the ones after it are
        //executed by Schedulers.computation(). Upstream operators before observeOn() are not impacted, but
        //downstream ones are

        //Happens on IO Scheduler
        Flowable.just("WHISKEY/27653/TANGO", "6555/BRAVO", "232352/5675675/FOXTROT")
                .subscribeOn(Schedulers.io())
                .flatMap(s -> Flowable.fromArray(s.split("/")))
                .doOnNext(s -> System.out.println("Split out " + s + " on thread "
                        + Thread.currentThread().getName()))
                //Happens on Computation Scheduler
                .observeOn(Schedulers.computation())
                .filter(s -> s.matches("[0-9]+"))
                .map(Integer::valueOf)
                .reduce((total, next) -> total + next)
                .doOnSuccess(i -> System.out.println("Calculated sum " + i + " on thread "
                        + Thread.currentThread().getName()))
                .subscribe(i -> System.out.println("Received " + i + " on thread "
                        + Thread.currentThread().getName()));
        sleep(400000);
    }

    @Test
    public void test1(){
        //Happens on main thread
        Observable.just("WHISKEY/27653/TANGO", "6555/BRAVO", "232352/5675675/FOXTROT")
                .flatMap(s -> Observable.fromArray(s.split("/")))
                .doOnNext(s -> System.out.println("Split out " + s + " on thread "
                        + Thread.currentThread().getName()))
                //Happens on Computation Scheduler
                .observeOn(Schedulers.computation())
                .filter(s -> s.matches("[0-9]+"))
                .map(Integer::valueOf)
                .reduce((total, next) -> total + next)
                .doOnSuccess(i -> System.out.println("Calculated sum " + i + " on thread "
                        + Thread.currentThread().getName()))
                .subscribe(i -> System.out.println("Received " + i + " on thread "
                        + Thread.currentThread().getName()));

        sleep(1000);
    }

    @Test
    public void test2() {
        //Happens on IO Scheduler
        Observable.just("WHISKEY/27653/TANGO", "6555/BRAVO", "232352/5675675/FOXTROT")
                .subscribeOn(Schedulers.io())
                .flatMap(s -> Observable.fromArray(s.split("/")))
                .doOnNext(s -> System.out.println("Split out " + s + " on thread "
                        + Thread.currentThread().getName()))
                //Happens on Computation Scheduler
                .observeOn(Schedulers.computation())
                .filter(s -> s.matches("[0-9]+"))
                .map(Integer::valueOf)
                .reduce((total, next) -> total + next)
                .doOnSuccess(i -> System.out.println("Calculated sum " + i + " on thread "
                        + Thread.currentThread().getName()))
                //Switch back to IO Scheduler
                .observeOn(Schedulers.io())
                .map(i -> i.toString())
                .doOnSuccess(s -> System.out.println("Writing " + s + " to file on thread "
                        + Thread.currentThread().getName()))
                .subscribe(s -> write(s, "/home/output.txt"));
        sleep(1000);
    }



    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void write(String text, String path) {
        BufferedWriter writer = null;
        try {
            //create a temporary file
            File file = new File(path);
            writer = new BufferedWriter(new FileWriter(file));
            writer.append(text);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }
}
