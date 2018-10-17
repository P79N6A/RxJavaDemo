package com.taobao;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Completable is simply concerned with an action being executed, but it does not receive any emissions.
 * Logically, it does not have onNext() or onSuccess() to receive emissions, but it does have onError() and
 * onComplete()
 *
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/16 上午9:41
 */
public class CompletableTest {

    @Test
    public void test() throws InterruptedException {
        Completable.fromRunnable(() -> runProcess()).subscribeOn(Schedulers.io()).subscribe(() -> System.out.println("Done!"));

        TimeUnit.SECONDS.sleep(5);
    }

    private void runProcess() {
        System.out.println("process...");
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("process end");
    }
}
