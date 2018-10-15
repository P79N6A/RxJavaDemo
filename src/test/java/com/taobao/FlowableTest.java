package com.taobao;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/28 下午6:53
 */
public class FlowableTest {

    @Test
    public void test() throws InterruptedException {
        Flowable<Integer> f1 = Flowable.just(1, 2, 3, 4).delay(40, TimeUnit.MILLISECONDS, Schedulers.io());
        Flowable<Integer> f2 = Flowable.just(9, 5, 6, 7).delay(60, TimeUnit.MILLISECONDS, Schedulers.io());

        List<Flowable<Integer>> flowableList = new ArrayList<>();
        flowableList.add(f1);
        flowableList.add(f2);

        Flowable<Flowable<Integer>> f3 = Flowable.fromIterable(flowableList);
        Flowable<Integer> f4 = f3.flatMap(flowable -> flowable);
        Flowable<List<Integer>> f5 = f4.toList().toFlowable();
        f5.blockingSubscribe(results -> {
            results.forEach(result -> {
                System.out.println(Thread.currentThread().getName() + "\t" + result);
            });
        });

    }
}
