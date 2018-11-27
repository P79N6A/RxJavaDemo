package com.taobao;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/11/19 下午4:37
 */
public class SubjectTest {

    @Test
    public void test() {

        Subject<String> subject = PublishSubject.create();

        subject.map(String::length).subscribe(System.out::println);

        subject.onNext("Alpha");
        subject.onNext("Beta");
        subject.onNext("Gamma");
        subject.onComplete();
    }

    @Test
    public void test1(){
        Observable<String> source1 =
                Observable.interval(1, TimeUnit.SECONDS)
                        .map(l -> (l + 1) + " seconds");
        Observable<String> source2 =
                Observable.interval(300, TimeUnit.MILLISECONDS)
                        .map(l -> ((l + 1) * 300) + " milliseconds");
        Subject<String> subject = PublishSubject.create();
        subject.subscribe(System.out::println);
        source1.subscribe(subject);
        source2.subscribe(subject);
        sleep(3000);
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
