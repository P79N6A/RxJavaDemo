package com.taobao;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/8/30 上午7:00
 */
public class Timer {

    @Test
    public void test() {
        Observable<Long> observable = Observable.timer(5, TimeUnit.SECONDS);
        observable.blockingSubscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Long aLong) {
                System.out.println(aLong);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                System.out.println("Complete");
            }
        });
    }
}
