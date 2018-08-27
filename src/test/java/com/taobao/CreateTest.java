package com.taobao;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/8/27 上午6:29
 */
public class CreateTest {

    @Test
    public void test() throws InterruptedException {

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        ObservableOnSubscribe<Integer> handler = emitter -> {

            Future future = executor.scheduleAtFixedRate(() -> {
                if (!emitter.isDisposed()) {
                    for (int i = 0; i < 10; i++) {
                        emitter.onNext(i);
                    }
                    emitter.onComplete();
                }
            }, 0, 1L, TimeUnit.SECONDS);

            emitter.setCancellable(() -> future.cancel(false));

        };

        Observable<Integer> observable = Observable.create(handler);

        observable.subscribe(
                item -> System.out.println(item),
                throwable -> System.out.println(throwable.getMessage()),
                () -> System.out.println("Done")
        );

        Thread.sleep(4000);
        executor.shutdown();


    }
}
