package com.taobao;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/11/20 上午11:06
 */
public class ConcurrencyTest {

    @Test
    public void test() {
        Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .map(s -> intenseCalculation((s)))
                .subscribe(System.out::println);
        Observable.range(1, 6)
                .map(s -> intenseCalculation((s)))
                .subscribe(System.out::println);
    }

    @Test
    public void test1() {
        //We can achieve this using the subscribeOn() operator, which suggests to the source to fire emissions on a
        //specified Scheduler
        Flowable.just("Alpha")
                .subscribeOn(Schedulers.computation())
                .filter(s -> s.length() > 2)
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println("Receive:" + s);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


        sleep(20000);
    }

    @Test
    public void test2() {
        //Something else that is exciting about RxJava is its operators (at least the official ones and the custom ones
        //built properly). They can work with Observables on different threads safely. Even operators and factories
        //that combine multiple Observables, such as merge() and zip(), will safely combine emissions pushed by
        //different threads
        Observable<String> source1 =
                Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                        .subscribeOn(Schedulers.computation())
                        .map(s -> intenseCalculation((s)));
        Observable<Integer> source2 =
                Observable.range(1, 6)
                        .subscribeOn(Schedulers.computation())
                        .map(s -> intenseCalculation((s)));
        Observable.zip(source1, source2, (s, i) -> s + "-" + i)
                .subscribe(System.out::println);
        sleep(50000);
    }

    public static <T> T intenseCalculation(T value) {
        sleep(ThreadLocalRandom.current().nextInt(3000));
        return value;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
