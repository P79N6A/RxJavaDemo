package com.taobao;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiPredicate;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
    public void range() {
//        To emit a consecutive range of integers, you can use Observable.range(). This will emit each number from a
//        start value and increment each emission until the specified count is reached. These numbers are all passed
//        through the onNext() event, followed by the onComplete() event

//        Note closely that the two arguments for Observable.range() are not lower/upper bounds. The first argument is
//        the starting value. The second argument is the total count of emissions, which will include both the initial
//        value and incremented values

        Observable observable = Observable.range(0, 10);
        observable.subscribe(s -> System.out.println("Receive 1:" + s));
        observable.subscribe(s -> System.out.println("Receive 2:" + s));


    }

    @Test
    public void interval() throws InterruptedException {
//        Observable.interval() will emit infinitely at the specified interval (which is 1 second in this case). However,
//        because it operates on a timer, it needs to run on a separate thread and will run on the computation
//        Scheduler by default.

        Observable observable = Observable.interval(1, TimeUnit.SECONDS);
        observable.subscribe(s -> System.out.println(s + " Observer 1"));
        TimeUnit.SECONDS.sleep(3);

//        Look what happened after five seconds elapsed, when Observer 2 came in. Note that it is on its own
//        separate timer and starting at 0! These two observers are actually getting their own emissions, each
//        starting at 0. So this Observable is actually cold

        observable.subscribe(s -> System.out.println(s + " Observer 2"));
        TimeUnit.SECONDS.sleep(5);

        //use ConnectableObservable to force these emissions to become hot
        ConnectableObservable seconds = observable.publish();
        //observer 1
        seconds.subscribe(l -> System.out.println("ConnectableObservable 1: " + l));
        seconds.connect();

        TimeUnit.SECONDS.sleep(5);
        //observer 2
        seconds.subscribe(l -> System.out.println("ConnectableObservable 2: " + l));
        TimeUnit.SECONDS.sleep(5);


    }

    @Test
    public void backpressure() {
        //The program above (with observeOn() operator commented out) runs just fine because it has accidental backpressure.
        // By default everything is single threaded in RxJava, thus producer and consumer work within the same thread.
        // Invoking subscriber.onNext() actually blocks, so the while loop throttles itself automatically
        Observable.create(emitter -> {
            long state = 0;
            while (!emitter.isDisposed()) {
                emitter.onNext(state++);
            }
        }).subscribe(i -> {
            TimeUnit.MILLISECONDS.sleep(1);
            System.out.println(i);
        });


    }

    @Test
    public void backpressure2() {
        //增加observeOn,运行时间长会导致异常
        Observable.create(emitter -> {
            long state = 0;
            while (!emitter.isDisposed()) {
                emitter.onNext(state++);
            }
        }).observeOn(Schedulers.io())
                .subscribe(i -> {
                    TimeUnit.MILLISECONDS.sleep(1);
                    System.out.println(i);
                }, throwable -> throwable.printStackTrace());
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
        subscribe(observable.retry(((integer, throwable) -> {
            //可以根据重试次数integer和异常类型throwable觉决定是否需要重试
            System.out.println(integer);
            return true;
        })));

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
