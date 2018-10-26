package com.taobao;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/30 下午2:06
 */
public class SingleTest {

    @Test
    public void test() {
        System.out.println(Single.timer(3, TimeUnit.SECONDS).doOnSuccess(i -> {
            System.out.println("触发了任务");
        }).blockingGet());
    }


    @Test
    public void compose() {
        Single.just(1).compose(upstream -> upstream.map(i -> "String:" + String.valueOf(i))).subscribe(System.out::println);
    }

    @Test
    public void concat() {
        //onComplete won’t be invoked after onSuccess
        Single.just(1).concatWith(Single.just(2)).blockingSubscribe(i -> {
            System.out.println(i);
        });
    }

    @Test
    public void filter() {
//        The filter() operator accepts Predicate<T> for a given Observable<T>. This means that you provide it a lambda
//        that qualifies each emission by mapping it to a Boolean value, and emissions with false will not go
//        forward.

        Single.create(emitter -> {
            if (new Random().nextInt(10) % 2 == 0) {
                emitter.onSuccess(true);
            } else {
                emitter.onSuccess(false);
            }
        }).filter(result -> (boolean) result).subscribe(System.out::println, throwable -> throwable.printStackTrace(), () -> System.out.println("Complete"));
    }

    @Test
    public void error() {

        Single.create(emitter -> emitter.onSuccess(true))
                .doOnError(e -> e.printStackTrace())
                .doOnSuccess(success -> System.out.println(success))
                .onErrorReturnItem(false).filter(s -> (boolean) s).subscribe(s -> System.out.println(s));
    }

    @Test
    public void toMaybe() {
        Single.create(emitter -> {
            if (new Random().nextInt() % 2 == 0) {
                emitter.onSuccess(true);
            }
        }).toMaybe().subscribe(System.out::println);
    }

}
