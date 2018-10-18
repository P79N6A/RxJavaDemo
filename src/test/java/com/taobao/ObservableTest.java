package com.taobao;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiPredicate;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

        length(observable);


    }

    @Test
    public void fromIterable() {
        List<String> items = Arrays.asList("Hello", "World");
        Observable<String> observable = Observable.fromIterable(items);
        length(observable);

    }

    @Test
    public void retry() {
        //无限次重试直至成功
        Observable observable = Observable.create(emitter -> {
            if (new Random().nextInt(10) % 2 == 0) {
                System.out.println("错误发生了");
                emitter.onError(new Exception("错误发生了"));
            } else {
                emitter.onNext(true);
            }
        });

        //没有重试
        System.out.println("===========没有重试============");
        subscribe(observable);

        //无限次重试直至成功
        System.out.println("===========无限重试============");
        subscribe(observable.retry());


        System.out.println("===========最多重试N次数============");
        subscribe(observable.retry(2));

        System.out.println("===========重试 BiPredicate===========");
        subscribe(observable.retry(new BiPredicate<Integer, Throwable>() {
            @Override
            public boolean test(Integer integer, Throwable throwable) throws Exception {
                System.out.println(integer);
                return true;
            }
        }));

    }

    private void subscribe(Observable observable) {
        observable.subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object o) {
                System.out.println("Receive:" + o);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("error:" + e.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("complete");
            }
        });
    }

    private void length(Observable<String> observable) {
        //Internally, it will act as an intermediary Observer and convert each string to its length().
        //This, in turn, will call onNext() on filter() to pass that integer, and the lambda condition i -> i >= 5
        //will suppress emissions that fail to be at least five characters in length. Finally, the filter() operator will
        //call onNext() to hand each item to the final Observer where they will be printed
        Observable<Integer> observableMap = observable.map(String::length);
        Observable observableFilter = observableMap.filter(i -> i >= 2);
        observableFilter.subscribe(i -> System.out.println("RECEIVE:" + i));
    }
}
