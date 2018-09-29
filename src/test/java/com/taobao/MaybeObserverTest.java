package com.taobao;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/28 下午2:31
 */
public class MaybeObserverTest {

    @Test
    public void test() throws InterruptedException {

        Maybe.just(isLogin()).subscribeOn(Schedulers.io()).subscribe(new MaybeObserver<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(Boolean aBoolean) {
                System.out.println("onSuccess" + Thread.currentThread());
                System.out.println(aBoolean);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        TimeUnit.SECONDS.sleep(10);
    }

    private boolean isLogin() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("isLogin:" + Thread.currentThread());
        return true;
    }
}
