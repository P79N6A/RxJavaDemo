package com.taobao.creating;

import io.reactivex.Observable;
import org.junit.Test;

import java.io.IOException;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/8/30 上午7:18
 */
public class ErrorTest {

    @Test
    public void test(){
        Observable<String> observable = Observable.fromCallable(() -> {
            if (Math.random() < 0.5) {
                throw new IOException();
            }
            throw new IllegalArgumentException();
        });

        //合并来上一个Observable，对异常进行来包装
        Observable<String> result = observable.onErrorResumeNext(error -> {
            if (error instanceof IllegalArgumentException) {
                return Observable.empty();
            }
            return Observable.error(error);
        });

        for (int i = 0; i < 10; i++) {
            result.subscribe(
                    v -> System.out.println("This should never be printed!"),
                    error -> error.printStackTrace(),
                    () -> System.out.println("Done"));
        }
    }

    @Test
    public void testSimple(){
        Observable<String> error = Observable.error(new IOException());

        error.subscribe(
                v -> System.out.println("This should never be printed!"),
                throwable -> throwable.printStackTrace(),
                () -> System.out.println("This neither!"));
    }
}
