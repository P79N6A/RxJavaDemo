package com.taobao;

import io.reactivex.Flowable;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/28 上午10:33
 */
public class BackpressureTest {

    @Test
    public void test() {
        Flowable.range(0,10).subscribe(new Subscriber<Integer>() {

            Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println("onSubscribe start!");
                this.subscription = subscription;
                this.subscription.request(1);
                System.out.println("onSubscribe end!");
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("onNext:" + integer);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete!");
            }
        });
    }

    /**
     * 响应式拉取
     */
    @Test
    public void reactivePull() {

    }
}
