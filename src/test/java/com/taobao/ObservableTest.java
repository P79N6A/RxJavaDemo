package com.taobao;

import io.reactivex.Observable;
import org.junit.Test;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/17 上午9:47
 */
public class ObservableTest {

    @Test
    public void create() {
        Observable<String> observable = Observable.create(emitter -> {
            emitter.onNext("Hello");
            emitter.onNext("World");
            emitter.onComplete();
        });

        //Internally, it will act as an intermediary Observer and convert each string to its length().
        //This, in turn, will call onNext() on filter() to pass that integer, and the lambda condition i -> i >= 5
        //will suppress emissions that fail to be at least five characters in length. Finally, the filter() operator will
        //call onNext() to hand each item to the final Observer where they will be printed
        Observable<Integer> observableMap = observable.map(String::length);

        Observable observableFilter = observableMap.filter(i -> i >= 2);

        observableFilter.subscribe(i -> System.out.println("RECEIVE:" + i));


    }
}
