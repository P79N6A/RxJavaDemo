package com.taobao;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/8/25 下午4:09
 */
public class HelloRxJava {

    public static void main(String[] args) {
        Observable.just("Hello World").subscribe(
                s -> System.out.println(s),
                throwable -> System.out.println(throwable.getMessage()),
                () -> System.out.println("complete"),
                disposable -> System.out.println("disposable"));

        System.out.println("===============Observer======================");

        Observable.just("Hello World").subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("disposable " + d.isDisposed());
            }

            @Override
            public void onNext(String s) {
                System.out.println(s);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("complete");
            }
        });
    }

}
