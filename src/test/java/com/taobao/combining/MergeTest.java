package com.taobao.combining;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Combines multiple Observables into one
 *
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/8/31 上午7:19
 */
public class MergeTest {


    @Test
    public void test() {

        Observable<Long> intervalRange = Observable.intervalRange(1, 20, 0, 1, TimeUnit.SECONDS);

        Observable<Long> observable = Observable.intervalRange(21, 20, 0, 1, TimeUnit.SECONDS);

        observable.mergeWith(intervalRange).blockingSubscribe(System.out::println);
    }

    @Test
    public void diffConcat() {
        Observable.merge(
                Observable.intervalRange(1,10,0,100, TimeUnit.MILLISECONDS).map(id -> "A" + id),
                Observable.intervalRange(1,10,0,100, TimeUnit.MILLISECONDS).map(id -> "B" + id))
                .blockingSubscribe(System.out::print);

        System.out.println();
        System.out.println("===============concat=================");

        Observable.concat(
                Observable.intervalRange(1,10,0,100, TimeUnit.MILLISECONDS).map(id -> "A" + id),
                Observable.intervalRange(1,10,0,100, TimeUnit.MILLISECONDS).map(id -> "B" + id))
                .blockingSubscribe(System.out::print);
    }

    @Test
    public void testError() {
        Observable<Long> intervalRange = Observable.intervalRange(1, 20, 0, 1, TimeUnit.SECONDS);

        Observable<Long> observable = Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> emitter) throws Exception {

                for (long i = 20; i < 30; i++) {
                    if (i == 25) {
                        emitter.onError(new Exception("i is 25"));
                    }
                    emitter.onNext(i);
                }

                emitter.onComplete();
            }
        });

        Observable.mergeDelayError(intervalRange, observable).blockingSubscribe(System.out::println, throwable -> throwable.printStackTrace());

    }
}
