package com.taobao;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/15 上午10:00
 */
public class DisposableTest {

    @Test
    public void testSingle() throws InterruptedException {
        Disposable d = Single.just("Hellow World").delay(1, TimeUnit.SECONDS, Schedulers.io()).subscribeWith(new DisposableSingleObserver<String>() {

            @Override
            public void onStart() {
                System.out.println("Started");
            }

            @Override
            public void onSuccess(String value) {
                System.out.println("Success: " + value);
            }

            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        });

        Thread.sleep(5000);

        d.dispose();
    }
}
