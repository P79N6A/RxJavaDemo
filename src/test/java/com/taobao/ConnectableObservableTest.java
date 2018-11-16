package com.taobao;

import io.reactivex.Observable;
import io.reactivex.observables.ConnectableObservable;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/23 上午9:46
 */
public class ConnectableObservableTest {

    @Test
    public void test() throws InterruptedException {

        //A helpful form of hot Observable is ConnectableObservable. It will take any Observable, even if it is cold, and
        //make it hot so that all emissions are played to all Observers at once. To do this conversion, you simply
        //need to call publish() on any Observable, and it will yield a ConnectableObservable. But subscribing will not start
        //the emissions yet. You need to call its connect() method to start firing the emissions. This allows you to set
        //up all your Observers beforehand

        ConnectableObservable<Long> source = Observable.interval(1, TimeUnit.SECONDS).map(i -> i * i).publish();

        //第一个订阅
        source.subscribe(s -> System.out.println("Observer 1:" + s), throwable -> throwable.printStackTrace(), () -> System.out.println("Observer 1 complete"));

        TimeUnit.SECONDS.sleep(3);

        //You need to call its connect() method to start firing the emissions,这个时候是个hot Observables
        source.connect();

        //Using ConnectableObservable will force emissions from the source to become hot, pushing a single stream of
        //emissions to all Observers at the same time rather than giving a separate stream to each Observer

        TimeUnit.SECONDS.sleep(3);

        //第二个订阅
        source.subscribe(s -> System.out.println("Observer 2:" + s), throwable -> throwable.printStackTrace(), () -> System.out.println("Observer 3 complete"));

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void threeRandoms() {

        //To see how multicasting works within a chain of operators, we are going to use Observable.range() and then
        //map each emission to a random integer. Since these random values will be nondeterministic and different
        //for each subscription, this will provide a good means to see whether our multicasting is working because
        //Observers should receive the same numbers.
        Observable<Integer> threeRandoms = Observable.range(1, 3).doOnNext(i -> System.out.println("DoOnNext")).map(i -> randomInt());
        threeRandoms.subscribe(i -> System.out.println("Observer 1: " + i));
        threeRandoms.subscribe(i -> System.out.println("Observer 2: " + i));


    }

    @Test
    public void threeRandoms2() {
        //This occurred because we multicast after Observable.range(), but the multicasting happens before the map()
        //operator. Even though we consolidated to one set of emissions coming from Observable.range(), each Observer
        //is still going to get a separate stream at map(). Everything before publish() was consolidated into a single
        //stream (or more technically, a single proxy Observer). But after publish(), it will fork into separate streams
        //for each Observer again
        ConnectableObservable<Integer> threeInts = Observable.range(1, 3).doOnNext(i -> System.out.println("DoOnNext")).publish();
        Observable<Integer> threeRandoms = threeInts.map(i -> randomInt());
        threeRandoms.subscribe(i -> System.out.println("Observer 1: " + i));
        threeRandoms.subscribe(i -> System.out.println("Observer 2: " + i));
        threeInts.connect();


    }

    @Test
    public void threeRandoms3() {
        ConnectableObservable<Integer> threeInts = Observable.range(1, 3).map(i -> randomInt()).publish();
        threeInts.subscribe(i -> System.out.println("Observer 1: " + i));
        threeInts.subscribe(i -> System.out.println("Observer 2: " + i));

        threeInts.connect();
    }

    @Test
    public void threeRandoms4() {
        ConnectableObservable<Integer> threeRandoms = Observable.range(1, 3).map(i -> randomInt()).publish();
//Observer 1 - print each random integer
        threeRandoms.subscribe(i -> System.out.println("Observer 1: " + i));
//Observer 2 - sum the random integers, then print
        threeRandoms.reduce(0, (total, next) -> total + next).subscribe(i -> System.out.println("Observer 2: " + i));
        threeRandoms.connect();
    }

    @Test
    public void autoConnect() {
        //The autoConnect() operator on ConnectableObservable can be quite handy. For a given ConnectableObservable<T>,
        //calling autoConnect() will return an Observable<T> that will automatically call connect() after a specified
        //number of Observers are subscribed. Since our previous example had two Observers, we can streamline
        //it by calling autoConnect(2) immediately after publish()

        //Obviously, this does not work well when you have an unknown number of Observers and you
        //want all of them to receive all emissions

        Observable<Integer> threeRandoms = Observable.range(1, 3)
                .map(i -> randomInt())
                .publish()
                .autoConnect(2);
        threeRandoms.subscribe(i -> System.out.println("Observer 1: " + i));
        threeRandoms.subscribe(i -> System.out.println("Observer 2: " + i));

        //If we add a third Observer to our example but keep autoConnect() specified at 2 instead of 3, it is
        //likely that the third Observer is going to miss the emissions
        threeRandoms.subscribe(i -> System.out.println("Observer 3: " + i));

    }

    @Test
    public void autoConnect2() {
        //Note that if you pass no argument for numberOfSubscribers, it will default to 1
        Observable<Long> seconds =
                Observable.interval(1, TimeUnit.SECONDS)
                        .publish()
                        .autoConnect();
//Observer 1
        seconds.subscribe(i -> System.out.println("Observer 1: " + i));
        sleep(3000);
//Observer 2
        seconds.subscribe(i -> System.out.println("Observer 2: " + i));
        sleep(3000);

        //If you pass 0 to autoConnect() for the numberOfSubscribers argument, it will start firing immediately and not wait
        //for any Observers. This can be handy to start firing emissions immediately without waiting for any
        //Observers.
    }


    @Test
    public void refCount() {
        //The refCount() operator on ConnectableObservable is similar to autoConnect(1), which fires after getting one
        //subscription. But there is one important difference; when it has no Observers anymore, it will dispose of
        //itself and start over when a new one comes in. It does not persist the subscription to the source when it
        //has no more Observers, and when another Observer follows, it will essentially "start over"

        Observable<Long> seconds =
                Observable.interval(1, TimeUnit.SECONDS)
                        .publish()
                        .refCount();
//Observer 1
        seconds.take(5)
                .subscribe(l -> System.out.println("Observer 1: " + l));
        sleep(3000);
//Observer 2
        seconds.take(3)
                .subscribe(l -> System.out.println("Observer 2: " + l));
        sleep(3000);
//there should be no more Observers at this point

//Observer 3
        seconds.subscribe(l -> System.out.println("Observer 3: " + l));
        sleep(3000);

        //You can also use an alias for publish().refCount() using the share() operator
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void replay() {
        //The replay() operator is a powerful way to hold onto previous emissions within a certain scope and reemit
        //them when a new Observer comes in. It will return a ConnectableObservable that will both multicast
        //emissions as well as emit previous emissions defined in a scope. Previous emissions it caches will fire
        //immediately to a new Observer so it is caught up, and then it will fire current emissions from that point
        //forward.

        Observable<Long> seconds =
                Observable.interval(1, TimeUnit.SECONDS)
                        .replay(2)
                        .autoConnect();
//Observer 1
        seconds.subscribe(l -> System.out.println("Observer 1: " + l));
        sleep(3000);
//Observer 2
        seconds.subscribe(l -> System.out.println("Observer 2: " + l));
        sleep(3000);

        seconds.subscribe(l -> System.out.println("Observer 3: " + l));
        sleep(3000);

        //replay() will keep caching all emissions it receives. If the source is
        //infinite or you only care about the last previous emissions, you might want to specify a bufferSize argument
        //to limit only replaying a certain number of last emissions. If we called replay(2) on our second Observer to
        //cache the last two emissions
    }

    @Test
    public void replay2() {
        //There are other overloads for replay(), particularly a time-based window you can specify
        Observable<Long> seconds =
                Observable.interval(300, TimeUnit.MILLISECONDS)
                        .map(l -> (l + 1) * 300) // map to elapsed milliseconds
                        .replay(1, TimeUnit.SECONDS)
                        .autoConnect();
//Observer 1
        seconds.subscribe(l -> System.out.println("Observer 1: " + l));
        sleep(2000);
//Observer 2
        seconds.subscribe(l -> System.out.println("Observer 2: " + l));
        sleep(1000);
    }


    @Test
    public void replay3() {
        //You can also specify a bufferSize argument on top of a time interval, so only a certain number of last
        //emissions are buffered within that time period
        Observable<Long> seconds =
                Observable.interval(300, TimeUnit.MILLISECONDS)
                        .map(l -> (l + 1) * 300) // map to elapsed milliseconds
                        .replay(1, 1, TimeUnit.SECONDS)
                        .autoConnect();
//Observer 1
        seconds.subscribe(l -> System.out.println("Observer 1: " + l));
        sleep(2000);
//Observer 2
        seconds.subscribe(l -> System.out.println("Observer 2: " + l));
        sleep(1000);
    }

    @Test
    public void cache() {
        //When you want to cache all emissions indefinitely for the long term and do not need to control the
        //subscription behavior to the source with ConnectableObservable, you can use the cache() operator. It will
        //subscribe to the source on the first downstream Observer that subscribes and hold all values indefinitely
        //没有cache，两次计算不一样
        Observable<Integer> noCachedRollingTotals =
                Observable.range(1, 3).map(i -> randomInt())
                        .scan(0, (total, next) -> total + next);

        noCachedRollingTotals.subscribe(i -> System.out.println("Observer 1:" + i));
        noCachedRollingTotals.subscribe(i -> System.out.println("Observer 2:" + i));

        System.out.println("===============cache================");
        Observable<Integer> cachedRollingTotals =
                Observable.range(1, 3).map(i -> randomInt())
                        .scan(0, (total, next) -> total + next).cache();

        cachedRollingTotals.subscribe(i -> System.out.println("Observer 1:" + i));
        cachedRollingTotals.subscribe(i -> System.out.println("Observer 2:" + i));

        //do not use cache() unless you really want to hold all elements indefinitely and do
        //not have plans to dispose it at any point. Otherwise, prefer replay() so you can more finely
        //control cache sizing and windows as well as disposal policies
    }

    @Test
    public void cache1() {
        //You can also call cacheWithInitialCapacity() and specify the number of elements to be expected in the cache
        Observable<Integer> cachedRollingTotals =
                Observable.just(6, 2, 5, 7, 1, 4, 9, 8, 3, 9)
                        .scan(0, (total, next) -> total + next)
                        .cacheWithInitialCapacity(2);

        cachedRollingTotals.subscribe(i -> System.out.println("Observer 1:" + i));
    }


    public static int randomInt() {
        return ThreadLocalRandom.current().nextInt(100000);
    }
}
