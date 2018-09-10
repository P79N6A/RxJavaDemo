package com.taobao.combining;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import org.junit.Test;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/4 上午7:21
 */
public class ZipTest {

    @Test
    public void test() {

        Observable<String> observable1 = Observable.just("A", "B", "C", "D");
        Observable<String> observable2 = Observable.just("1", "2", "3");

        Observable.zip(observable1, observable2, (s1, s2) -> s1 + s2).subscribe(System.out::println, Throwable::printStackTrace);
    }

    @Test
    public void error() {
        Observable<String> observable1 =Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("A");
                emitter.onNext("B");
                emitter.onNext("C");
                emitter.onError(new Exception("emitter error"));
            }
        });
        Observable<String> observable2 = Observable.just("1", "2", "3");

        Observable.zip(observable1, observable2, (s1, s2) -> s1 + s2).subscribe(System.out::println, Throwable::printStackTrace);
    }
}
