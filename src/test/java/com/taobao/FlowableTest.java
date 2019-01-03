package com.taobao;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/28 下午6:53
 */
public class FlowableTest {


    @Test
    public void repeat() {
//        The repeat() operator will repeat subscription upstream after onComplete() a specified number of times
//        If you do not specify a number, it will repeat infinitely, forever re-subscribing after every onComplete().
//         there is also a repeatUntil() operator that accepts a Boolean Supplier lambda argument and will continue
//         repeating until it yields true.
        Flowable.just(1, 2, 3).repeat(2).subscribe(new FlowableSubscriber<Integer>() {

            private Subscription s;

            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                this.s.request(1);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("Receive :" + integer);
                s.request(1);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("complete");
            }
        });
    }

    @Test
    public void scan() {
//        The scan() method is a rolling aggregator. For every emission, you add it to an accumulation. Then, it will
//        emit each incremental accumulation.

        Flowable.just(1, 2, 3, 4).scan((accumulator, next) -> accumulator * next).subscribe(System.out::println);

//        You can also provide an initial value for the first argument and aggregate into a different type than what is
//        being emitted. If we wanted to emit the rolling count of emissions, we can provide an initial value of 0
//        and just add 1 to it for every emission. Keep in mind that the initial value will be emitted first, so use
//        skip(1) after scan() if you do not want that initial emission
        Flowable.just("Alpha", "Beta", "Hangzhou", "Beijing").scan(0, (total, next) -> total + next.length()).skip(1).subscribe(System.out::println);
    }

    /********************************************Flowables and Backpressure****************************************/

    @Test
    public void backpressure() {
        //The outputted alternation between Constructing MyItem and Received MyItem shows that each emission is bring
        //processed one at a time from the source all the way to the terminal Observer. This is because one thread is
        //doing all the work for this entire operation, making everything synchronous. The consumers and producers
        //are passing emissions in a serialized, consistent flow
        Observable.range(1, 999)
                .map(MyItem::new)
                .subscribe(myItem -> {
                    sleep(50);
                    System.out.println("Received MyItem " + myItem.id);
                });
    }

    @Test
    public void backpressure1() {
        //When you add concurrency operations to an Observable chain (particularly observeOn(), parallelization, and
        //operators such as delay()), the operation become asynchronous. This means hat multiple parts of the
        //Observable chain can be processing emissions at a given time, and producers can outpace consumers as they
        //are now operating on different threads. An emission is no longer strictly being handed downstream one at
        //a time from the source all the way to the Observer before starting the next one
        Observable.range(1, 999)
                .map(MyItem::new)
                .observeOn(Schedulers.io())
                .subscribe(myItem -> {
                    sleep(50);
                    System.out.println("Received MyItem " + myItem.id);
                });

        sleep(Integer.MAX_VALUE);
    }

    static final class MyItem {
        final int id;

        MyItem(int id) {
            this.id = id;
            System.out.println("Constructing MyItem " + id);
        }
    }


    /********************************************Suppressing sources****************************************/

    @Test
    public void create() {
        Flowable.create((FlowableOnSubscribe<String>) emitter -> {
            emitter.onNext("Hello");
            emitter.onNext("World");
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER).subscribe(System.out::println);
    }

    @Test
    public void just() {
        Flowable.just("name", "ysk").map(this::mapper).subscribe(System.out::println);
    }

    @Test
    public void fromArray() {
        List<String> ls = new ArrayList<>();
        ls.add("Hello");
        ls.add("World");
        Flowable.fromArray(ls.toArray()).subscribe(System.out::println);
    }

    @Test
    public void range() {
        //To emit a consecutive range of integers, you can use Observable.range(). This will emit each number from a
        //start value and increment each emission until the specified count is reached. These numbers are all passed
        //through the onNext() event, followed by the onComplete() event

        //Note closely that the two arguments for Observable.range() are not lower/upper bounds. The first argument is
        //the starting value. The second argument is the total count of emissions, which will include both the initial
        //value and incremented values
        Flowable.range(0, 10).subscribe(System.out::println);
    }


    @Test
    public void interval() throws InterruptedException {

        //However,because it operates on a timer, it needs to run on a separate thread and will run on the computation
        //Scheduler by default.
        Flowable.interval(1, TimeUnit.SECONDS).subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(4);
    }

    @Test
    public void interval1() {
        //Look what happened after five seconds elapsed, when Observer 2 came in. Note that it is on its own
        //separate timer and starting at 0! These two observers are actually getting their own emissions, each
        //starting at 0. So this Observable is actually cold
        Flowable<Long> seconds = Flowable.interval(1,
                TimeUnit.SECONDS);
//Observer 1
        seconds.subscribe(l -> System.out.println("Observer 1: " + l));
//sleep 5 seconds
        sleep(5000);
//Observer 2
        seconds.subscribe(l -> System.out.println("Observer 2: " + l));
//sleep 5 seconds
        sleep(5000);
    }

    @Test
    public void interval2() {
        //To put all observers on the same timer with the same
        //emissions, you will want to use ConnectableObservable to force these emissions to become hot
        ConnectableFlowable<Long> seconds = Flowable.interval(1, TimeUnit.SECONDS).publish();
        //observer 1
        seconds.subscribe(l -> System.out.println("Observer 1: " + l));
        seconds.connect();
//sleep 5 seconds
        sleep(5000);
//observer 2
        seconds.subscribe(l -> System.out.println("Observer 2: " + l));
//sleep 5 seconds
        sleep(5000);
    }

    @Test
    public void empty() {
        //Although this may not seem useful yet, it is sometimes helpful to create an Observable that emits nothing and
        //calls onComplete():
        //An empty Observable is essentially RxJava's concept of null. It is the absence of a value (or technically,
        //"values"). Empty Observables are much more elegant than nulls because operations will simply continue
        //empty rather than throw NullPointerExceptions
        Flowable empty = Flowable.empty();
        empty.subscribe(System.out::println, throwable -> {
            ((Throwable) throwable).printStackTrace();
        }, () -> System.out.print("Done"));
    }

    @Test
    public void error() {
        Flowable.error(new Exception("Crash and burn")).subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.print("Done"));
        //You can also provide the exception through a lambda so that it is created from scratch and separate
        //exception instances are provided to each Observer
        Flowable.error(() -> new Exception("Crash and burn")).subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.print("Done"));
    }

    @Test
    public void never() {
        //This Observable is primarily used for testing and not that often in production. We have to use sleep() here just
        //like Observable.interval() because the main thread is not going to wait for it after kicking it off
        Flowable<String> empty = Flowable.never();
        empty.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done!"));
        sleep(5000);
    }


    /********************************************Suppressing operators****************************************/

    @Test
    public void filter() {
        //The filter() operator accepts Predicate<T> for a given Observable<T>. This means that you provide it a lambda
        //that qualifies each emission by mapping it to a Boolean value, and emissions with false will not go
        //forward.
        //The filter() function is probably the most commonly used operator to suppress emissions.
        Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .filter(s -> s.length() != 5).subscribe(s -> System.out.println("RECEIVED: " + s));
    }

    @Test
    public void take() {
//        The take() operator has two overloads. One will take a specified number of emissions and then call
//        onComplete() after it captures all of them. It will also dispose of the entire subscription so that no more
//        emissions will occur
        Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .take(3)
                .doOnCancel(() -> System.out.println("Cancel"))
                .doOnComplete(() -> System.out.println("Complete"))
                .subscribe(s -> System.out.println("RECEIVED: " + s));

        //Note that if you receive fewer emissions than you specify in your take() function, it will simply emit what
        //it does get and then call the onComplete() function
    }

    @Test
    public void take1() {
        Flowable.interval(300, TimeUnit.MILLISECONDS)
                .take(2, TimeUnit.SECONDS)
                .subscribe(i -> System.out.println("RECEIVED: " + i));
        sleep(5000);
    }

    @Test
    public void takeLast() {
        //takeLast() operator, which will take the last specified number of emissions (or
        //time duration) before the onComplete() function is called. Just keep in mind that it will internally queue
        //emissions until its onComplete() function is called, and then it can logically identify and emit the last
        //emissions.
        Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .takeLast(3)
                .doOnComplete(() -> System.out.println("Complete"))
                .subscribe(s -> System.out.println("RECEIVED: " + s));
    }

    @Test
    public void takeWhile() {
//        Another variant of the take() operator is the takeWhile() operator, which takes emissions while a condition
//        derived from each emission is true
        Flowable.range(1, 100).takeWhile(i -> i < 5).subscribe(i -> System.out.println("RECEIVE:" + i));
    }

    @Test
    public void skipWhile() {
//        there is a skipWhile() function. It will keep skipping emissions while they
//        qualify with a condition. The moment that condition no longer qualifies, the emissions will start going
//        through
        Flowable.range(0, 100).skipWhile(i -> i < 95).subscribe(i -> System.out.println("RECEIVE: " + i));
    }

    @Test
    public void skip() {
//        The skip() operator does the opposite of the take() operator. It will ignore the specified number of
//        emissions and then emit the ones that follow
        Flowable<Integer> flowable = Flowable.range(0, 100);
        flowable.skip(90).subscribe(i -> System.out.println("RECEIVE 1: " + i));
//        Just like the take() operator, there is also an overload accepting a time duration. There is also a skipLast()
//        operator, which will skip the last specified number of items (or time duration) before the onComplete() event
//        is called. Just keep in mind that the skipLast() operator will queue and delay emissions until it confirms the
//        last emissions in that scope.

        flowable.skipLast(90).subscribe(i -> System.out.println("RECEIVE 2: " + i));

    }

    @Test
    public void distinct() {
        //The distinct() operator will emit each unique emission, but it will suppress any duplicates that follow.
        //Equality is based on hashCode()/equals() implementation of the emitted objects

        Flowable<String> flowable = Flowable.just("Hello", "Hello", "World", "World");
        flowable.distinct().subscribe(i -> System.out.println("RECEIVE 1: " + i));

//        You can also add a lambda argument that maps each emission to a key used for equality logic. This allows
//        the emissions, but not the key, to go forward while using the key for distinct logic. For instance, we can
//        key off each string's length and use it for uniqueness, but emit the strings rather than their lengths

        flowable.distinct(String::length).subscribe(i -> System.out.println("RECEIVE 2: " + i));


    }

    @Test
    public void distinctUntilChanged() {

//        The distinctUntilChanged() function will ignore duplicate consecutive emissions. It is a helpful way to ignore
//        repetitions until they change. If the same value is being emitted repeatedly, all the duplicates will be
//        ignored until a new value is emitted. Duplicates of the next value will be ignored until it changes again,
//        and so on

        Flowable.just(1, 1, 2, 2, 3, 3, 4, 5, 4).distinctUntilChanged().subscribe(i -> System.out.println("RECEIVE :" + i));


    }

    @Test
    public void distinctUntilChanged1() {
        //you can provide an optional argument for a key through a lambda mapping
        Flowable.just("Alpha", "Beta", "Zeta", "Eta", "Gamma",
                "Delta")
                .distinctUntilChanged(String::length)
                .subscribe(i -> System.out.println("RECEIVED: " + i));
    }

    @Test
    public void elementAt() {
//        You can get a specific emission by its index specified by a Long, starting at 0. After that item is found and
//        emitted, onComplete() will be called and the subscription will be disposed of

        Flowable.just(1, 2, 3, 4).elementAt(2).subscribe(System.out::println, throwable -> throwable.printStackTrace(), () -> System.out.println("Complete"));
    }

    /********************************************Transforming operators****************************************/

    @Test
    public void map() {
//        For a given Observable<T>, the map() operator will transform a T emission into an R emission using the
//        provided Function<T,R> lambda
//        The map() operator does a one-to-one conversion for each emission. If you need to do a one-to-many
//        conversion (turn one emission into several emissions), you will likely want to use flatMap() or concatMap()


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yyyy");
        Flowable.just("1/3/2016", "5/9/2016", "10/12/2016")
                .map(s -> LocalDate.parse(s, dtf))
                .subscribe(i -> System.out.println("RECEIVED: " + i));

    }

    @Test
    public void cast() {
//        A simple, map-like operator to cast each emission to a different type is cast().
        Flowable.just(1, 2).cast(Integer.class).subscribe(System.out::println);
    }

    @Test
    public void startWith() {
//        the startWith() operator allows you to insert a T emission that precedes all the
//        other emissions
        Flowable.just("Coffee", "Tea").startWith("COFFEE SHOP MENU").subscribe(System.out::println);
    }

    @Test
    public void startWithArray() {
        //If you want to start with more than one emission, use startWithArray() to accept varargs parameters
        //The startWith() operator is helpful for cases like this, where we want to seed an initial value or precede
        //our emissions with one or more emissions.
        Flowable<String> menu =
                Flowable.just("Coffee", "Tea", "Espresso", "Latte");
//print menu
        menu.startWithArray("COFFEE SHOP MENU", "----------------")
                .subscribe(System.out::println);
    }

    @Test
    public void defaultIfEmpty() {

        //If we want to resort to a single emission if a given Observable comes out empty, we can use defaultIfEmpty()
        Flowable<String> items =
                Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");
        items.filter(s -> s.startsWith("Z"))
                .defaultIfEmpty("None")
                .doOnComplete(() -> System.out.println("Complete"))
                .subscribe(System.out::println);

    }

    @Test
    public void switchIfEmpty() {
        //Similar to defaultIfEmpty(), switchIfEmpty() specifies a different Observable to emit values from if the source
        //Observable is empty. This allows you specify a different sequence of emissions in the event that the source
        //is empty rather than emitting just one valu
        Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .filter(s -> s.startsWith("Z"))
                .switchIfEmpty(Flowable.just("Zeta", "Eta", "Theta"))
                .subscribe(i -> System.out.println("RECEIVED: " + i),
                        e -> System.out.println("RECEIVED ERROR: " + e)
                );
    }

    @Test
    public void sorted() {

        Flowable<Integer> flowable = Flowable.just(2, 4, 9, 8, 10);
//        If you have a finite Observable<T> emitting items that implement Comparable<T>, you can use sorted() to sort the
//        emissions. Internally, it will collect all the emissions and then re-emit them in their sorted order

//        Of course, this can have some performance implications as it will collect all emissions in memory before
//        emitting them again. If you use this against an infinite Observable, you may get an OutOfMemory error.
        flowable.sorted().subscribe(System.out::println);

//        You can also provide Comparator as an argument to specify an explicit sorting criterion. We can provide
//        Comparator to reverse the sorting order, such as the one shown as follows:
        flowable.sorted(Comparator.reverseOrder()).subscribe(System.out::println);
//        Since Comparator is a single-abstract-method interface, you can implement it quickly with a lambda. Specify
//        the two parameters representing two emissions, and then map them to their comparison operation
        Flowable.just("Hello", "Alpha", "Beta", "Epsilon").sorted(Comparator.comparing(String::length)).subscribe(System.out::println);


    }

    @Test
    public void delay() throws InterruptedException {
//        We can postpone emissions using the delay() operator. It will hold any received emissions and delay each
//        one for the specified time period
        Flowable.just(1, 2, 3, 4).delay(1, TimeUnit.SECONDS).subscribe(System.out::println);
//        Because delay() operates on a different scheduler (such as Observable.interval()), we need to leverage a
//        sleep() method to keep the application alive long enough to see this happen
        TimeUnit.SECONDS.sleep(3);
    }

    /********************************************Reducing operators****************************************/

    @Test
    public void count() {
//        The simplest operator to consolidate emissions into a single one is count(). It will count the number of
//        emissions and emit through a Single once onComplete() is called

//        Like most reduction operators, this should not be used on an infinite Observable. It will hang up and work
//        infinitely, never emitting a count or calling onComplete(). You should consider using scan() to emit a rolling
//        count instead.
        Flowable.just(1, 2, 3, 4).count().subscribe(System.out::print);
    }

    @Test
    public void reduce() {

//        The reduce() operator is syntactically identical to scan(), but it only emits the final accumulation when the
//        source calls onComplete(). Depending on which overload you use, it can yield Single or Maybe

        Flowable.just(5, 3, 7, 10, 2, 14)
                .reduce("", (total, next) -> total + (total.equals("") ? "" :
                        ",") + next)
                .subscribe(s -> System.out.println("Received: " + s));

    }

    @Test
    public void all() {
//        The all() operator verifies that each emission qualifies with a specified condition and return a
//        Single<Boolean>. If they all pass, it will emit True. If it encounters one that fails, it will immediately emit False

        //If you call all() on an empty Observable, it will emit true due to the principle of vacuous
        //truth
        Flowable.just(5, 4, 6).all(i -> i > 2).subscribe(System.out::println);
    }

    @Test
    public void any() {
        //The any() method will check whether at least one emission meets a specific criterion and return a
        //Single<Boolean>. The moment it finds an emission that qualifies, it will emit true and then call onComplete(). If
        //it processes all emissions and finds that they all are false, it will emit false and call onComplete().

        //If you call any() on an empty Observable, it will emit false due to the principle of vacuous
        //truth
        Flowable.just(1, 2, 3).any(i -> i > 2).subscribe(System.out::println);
    }

    @Test
    public void contains() {
        //The contains() operator will check whether a specific element (based on the hashCode()/equals()
        //implementation) ever emits from an Observable. It will return a Single<Boolean> that will emit true if it is found
        //and false if it is not.
        Flowable.just(1, 2, 3, 4).contains(1).subscribe(System.out::println);
    }

    /********************************************Reducing operators****************************************/


    @Test
    public void toList() {
        //A common collection operator is toList(). For a given Observable<T>, it will collect incoming emissions into
        //a List<T> and then push that entire List<T> as a single emission (through Single<List<T>>).
        Flowable.just(1, 2, 3, 4).toList().subscribe(System.out::println);
        //If you want to specify a different list implementation besides ArrayList, you can provide a Callable lambda
        //as an argument to construct one. In the following code snippet, I provide a CopyOnWriteArrayList instance to
        //serve as my list
        Flowable.just(1, 2, 3, 4).toList(CopyOnWriteArrayList::new).subscribe(System.out::println);
    }

    @Test
    public void toSortedList() {
        //different flavor of toList() is toSortedList(). This will collect the emissions into a list that sorts the items
        //naturally based on their Comparator implementation. Then, it will emit that sorted List<T> forward to the
        //Observer

        //Like sorted(), you can provide a Comparator as an argument to apply a different sorting logic
        Flowable.just(2, 3, 10, 23, 87).toSortedList(Comparator.reverseOrder()).subscribe(System.out::println);
    }

    @Test
    public void toMap() {
        //For a given Observable<T>, the toMap() operator will collect emissions into Map<K,T>, where K is the key type
        //derived off a lambda Function<T,K> argument producing the key for each emission.
        Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon").toMap(s -> s.charAt(0)).subscribe(System.out::println);

        //If we wanted to yield a different value other than the emission to associate with the key, we can provide a
        //second lambda argument that maps each emission to a different value. We can, for instance, map each first
        //letter key with the length of that string:
        Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon").toMap(s -> s, s -> s.length()).subscribe(System.out::println);

        //By default, toMap() will use HashMap. You can also provide a third lambda argument that provides a different
        //map implementation. For instance, I can provide ConcurrentHashMap instead of HashMap
        Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon").toMap(s -> s, s -> s.length(), ConcurrentHashMap::new).subscribe(System.out::println);

        //that if I have a key that maps to multiple emissions, the last emission for that key is going to replace
        //subsequent ones. If I make the string length the key for each emission, Alpha is going to be replaced by
        //Gamma, which is going to be replaced by Delta
        Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon").toMap(s -> s.length()).subscribe(System.out::println);

    }

    @Test
    public void toMultiMap() {
        //If you want a given key to map to multiple emissions, you can use toMultiMap() instead, which will maintain
        //a list of corresponding values for each key. Alpha, Gamma, and Delta will then all be put in a list that is keyed
        //off the length five
        Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon").toMultimap(s -> s.length()).subscribe(System.out::println);
    }

    @Test
    public void collect() {
        //the collect() operator is helpful to collect emissions into any arbitrary type that RxJava does not
        //provide out of the box

        //When none of the collection operators have what you need, you can always use the collect() operator to
        //specify a different type to collect items into
        Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon").collect(HashSet::new, HashSet::add).subscribe(System.out::println);
    }


    private Function<String, String> mapper(String st) {
        return (s) -> "Hello " + s;
    }


    @Test
    public void time() throws InterruptedException {
        Flowable.timer(1, TimeUnit.SECONDS).subscribe(new FlowableSubscriber<Long>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(Long aLong) {

            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        });
        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    public void onErrorReturn() {

        //When you want to resort to a default value when an exception occurs, you can use onErrorReturnItem(). If we
        //want to emit -1 when an exception occurs, we can do it like this
        Flowable.just(2, 3, 5, 0, 9, 8).map(i -> 10 / i).onErrorReturnItem(-1).subscribe(System.out::println);

        //You can also supply Function<Throwable,T> to dynamically produce the value using a lambda. This gives you
        //access to Throwable , which you can use to determine the returned value as shown in the following code
        //snippet:
        Flowable.just(2, 3, 5, 0, 9, 8).map(i -> 10 / i).onErrorReturn(e -> -1).subscribe(System.out::println);

        //Note that even though we emitted -1 to handle the error, the sequence still terminated after that. We did not
        //get the 3, 2, or 8 that was supposed to follow
        Flowable.just(2, 3, 5, 0, 9, 8).map(i -> {
            try {
                return 10 / i;
            } catch (Exception ex) {
                return -1;
            }
        }).subscribe(System.out::println);

    }

    @Test
    public void onErrorResumeNext() {
        //onErrorResumeNext() is very similar. The only difference is
        //that it accepts another Observable as a parameter to emit potentially multiple values, not a single value, in
        //the event of an exception
        Flowable.just(2, 3, 5, 0, 9, 8).map(i -> 10 / i).onErrorResumeNext(Flowable.just(-1)).subscribe(System.out::println);
    }

    @Test
    public void retry() throws InterruptedException {
        //If you call retry() with no arguments, it will resubscribe an infinite number of times for each error. You
        //need to be careful with retry() as it can have chaotic effects
        Flowable.just(2, 3, 5, 0, 9, 8).map(i -> 10 / i).retry(1).subscribe(System.out::println, throwable -> System.out.println("错误了"));

        //The retryUntil() operator will allow retries while a given BooleanSupplier lambda is
        //false
        AtomicInteger count = new AtomicInteger(0);
        Flowable.just(2, 3, 5, 0, 9, 8).map(i -> 10 / i).retryUntil(() -> {
            int num = count.incrementAndGet();
            return num >= 1;
        }).subscribe(System.out::println, throwable -> System.out.println("错误了"));

        //There is also an advanced retryWhen() operator that supports advanced composition for tasks such as
        //delaying retries
        Flowable.just(2, 3, 5, 0, 9, 8).map(i -> 10 / i).retryWhen(throwableFlowable -> Flowable.timer(3, TimeUnit.SECONDS)).subscribe(System.out::println, throwable -> System.out.println("错误了"));


        TimeUnit.SECONDS.sleep(4);
    }


    @Test
    public void generate() {
        //generate 可以根据背压动态计算需要生产多少数据

        Flowable<Long> numbers = Flowable.generate(() -> 0L, (state, emitter) -> {
            System.out.println("generate 调用了");
            emitter.onNext(state);
            return state + 1;
        });

        numbers.observeOn(Schedulers.newThread()).subscribe(System.out::println);
    }


    @Test
    public void concatMap() {
        Flowable<String> items = Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon",
                "Zeta", "Eta", "Theta", "Iota");
//delay each String to emulate an intense calculation
        Flowable<String> processStrings = items.concatMap(s ->
                Flowable.just(s)
                        .delay(randomSleepTime(), TimeUnit.MILLISECONDS)
        );
        processStrings.subscribe(System.out::println);
//keep application alive for 20 seconds
        sleep(20000);
    }

    @Test
    public void switchMap() {
        //it will emit from the latest Observable derived from the latest emission and
        //dispose of any previous Observables that were processing. In other words, it allows you to cancel an
        //emitting Observable and switch to a new one, preventing stale or redundant processing
        //This can be helpful in many situations to prevent redundant
        //or stale work and is especially effective in user interfaces where rapid user inputs create stale requests.
        //You can use it to cancel database queries, web requests, and other expensive tasks and replace it with a
        //new task.
        Flowable<String> items = Flowable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon",
                "Zeta", "Eta", "Theta", "Iota");
//delay each String to emulate an intense calculation
        Flowable<String> processStrings = items.concatMap(s ->
                Flowable.just(s)
                        .delay(randomSleepTime(), TimeUnit.MILLISECONDS)
        );
        Flowable.interval(5, TimeUnit.SECONDS).switchMap(i -> processStrings.doOnCancel(() -> System.out.println("Disposing! Starting next"))).subscribe(System.out::println);
        sleep(20000);
        //For switchMap() to work effectively, the thread pushing emissions into switchMap() cannot be occupied doing
        //the work inside switchMap(). This means that you may have to use observeOn() or subscribeOn() inside switchMap()
        //to do work on a different thread. If the operations inside switchMap() are expensive to stop (for instance, a
        //database query using RxJava-JDBC), you might want to use unsubscribeOn() as well to keep the triggering
        //thread from becoming occupied with disposal.
    }

    public static int randomSleepTime() {
//returns random sleep time between 0 to 2000 milliseconds
        return ThreadLocalRandom.current().nextInt(2000);
    }


    @Test
    public void doOnNext() {
        //The doOnNext() operator allows you to peek at each emission coming out of an operator and going into the
        //next. This operator does not affect the operation or transform the emissions in any way. We just create a
        //side-effect for each event that occurs at that point in the chain
        Flowable.just("Alpha", "Beta", "Zeta", "Eta", "Gamma", "Delta").doOnNext(s -> {
            System.out.println("Processing: " + s);
            s += "hello";
        }).map(String::length).doOnNext(s -> System.out.println("Processing: " + s)).subscribe(System.out::println);
    }

    @Test
    public void doOnComplete() {
        //The onComplete() operator allows you to fire off an action when onComplete() is called at the point in the
        //Observable chain. This can be helpful in seeing which points of the Observable chain have completed
        Flowable.just("Alpha", "Beta", "Zeta", "Eta", "Gamma", "Delta").doOnComplete(() -> System.out.println("Source is done"))
                .map(String::length).doOnComplete(() -> System.out.println("Map is done"))
                .subscribe(System.out::println);
    }

    @Test
    public void doOnError() {
        Observable.just(5, 2, 4, 0, 3, 2, 8)
                .doOnError(e -> System.out.println("Source failed!"))
                .map(i -> 10 / i)
                .doOnError(e -> System.out.println("Division failed!"))
                .subscribe(i -> System.out.println("RECEIVED: " + i),
                        e -> System.out.println("RECEIVED ERROR: " + e)
                );
    }

    @Test
    public void doOnSubscribe() {
        //Two other helpful action operators are doOnSubscribe() and doOnDispose(). The doOnSubscribe() fires a specific
        //Consumer<Disposable> the moment subscription occurs at that point in the Observable chain. It provides access to
        //the Disposable in case you want to call dispose() in that action

        //The doOnDispose() operator will perform a
        //specific action when disposal is executed at that point in the Observable chain

        //Note that doOnDispose() can fire multiple times for redundant disposal requests or not at all if it is not
        //disposed of in some form or another

        Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .doOnSubscribe(d -> System.out.println("Subscribing!"))
                .doOnDispose(() -> System.out.println("Disposing!"))
                .subscribe(i -> System.out.println("RECEIVED: " + i));

    }

    @Test
    public void doOnSuccess() {
        //Remember that Maybe and Single types do not have an onNext() event but rather an onSuccess() operator to pass
        //a single emission. Therefore, there is no doOnNext() operator on either of these types, as observed in the
        //following code snippet, but rather a doOnSuccess() operator. Its usage should effectively feel like doOnNext()
        Observable.just(5, 3, 7, 10, 2, 14)
                .reduce((total, next) -> total + next)
                .doOnSuccess(i -> System.out.println("Emitting: " + i))
                .subscribe(i -> System.out.println("Received: " + i));
    }

    @Test
    public void doOnEach() {
        //You can specify all three actions for onNext(), onComplete(), and onError() using doOnEach() as
        //well. The subscribe() method accepts these three actions as lambda arguments or an entire
        //Observer<T>. It is like putting subscribe() right in the middle of your Observable chain
        Flowable.just("Alpha", "Beta", "Zeta", "Eta", "Gamma", "Delta").doOnEach(new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {

            }

            @Override
            public void onNext(String s) {
                System.out.println("Receive: " + s);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {
                System.out.println("Source complete");
            }
        }).subscribe(System.out::println);
    }


    /*************************************Buffering*****************************************************/

    @Test
    public void buffer() {
        //buffer() accepts a count argument that batches emissions in that fixed size. If we
        //wanted to batch up emissions into lists of eight elements, we can do that as follows

        //Of course, if the number of emissions does not cleanly divide, the remaining elements will be emitted in a
        //final list even if it is less than the specified count
        Flowable.range(1, 50).buffer(8).subscribe(System.out::println);
    }

    @Test
    public void buffer2() {
        //You can also supply a second bufferSupplier lambda argument to put items in another collection besides a
        //list, such as HashSet,
        Flowable.range(1, 50)
                .buffer(8, HashSet::new)
                .subscribe(System.out::println);
    }

    @Test
    public void buffer3() {
        //you can also provide a skip argument that specifies how many items
        //should be skipped before starting a new buffer. But if they
        //are different, you can get some interesting behaviors
        Flowable.range(1, 10).buffer(2, 3).subscribe(System.out::println);

        //If skip is equal to count, the skip has no effect
        Flowable.range(1, 10).buffer(2, 2).subscribe(System.out::println);

        //If you make skip less than count, you can get some interesting rolling buffers

        Flowable.range(1, 10).buffer(3, 1).subscribe(System.out::println);
    }

    @Test
    public void buffer4() {
        //You can use buffer() at fixed time intervals by providing a long and TimeUnit
        Flowable.interval(300, TimeUnit.MILLISECONDS).map(i -> (i + 1) * 300).buffer(1, TimeUnit.SECONDS).subscribe(System.out::println);
        sleep(8000);
    }

    @Test
    public void buffer5() {
        //There is an option to also specify a timeskip argument, which is the timer-based counterpart to skip. It
        //controls the timing of when each buffer starts
        Flowable.interval(300, TimeUnit.MILLISECONDS).map(i -> (i + 1) * 300).buffer(2, 1, TimeUnit.SECONDS).subscribe(System.out::println);
        sleep(4000);
    }

    @Test
    public void buffer6() {
        //You can also leverage a third count argument to provide a maximum buffer size. This will result in a buffer
        //emission at each time interval or when count is reached, whichever happens first. If the count is reached
        //right before the time window closes, it will result in an empty buffer being emitted.
        Flowable.interval(300, TimeUnit.MILLISECONDS).map(i -> (i + 1) * 300).buffer(1, TimeUnit.SECONDS, 2).subscribe(System.out::println);
        sleep(4000);
    }

    @Test
    public void buffer7() {
        //The most powerful variance of buffer() is accepting another Observable as a boundary argument. It does not
        //matter what type this other Observable emits. All that matters is every time it emits something, it will use the
        //timing of that emission as the buffer cut-off. In other words, the arbitrary occurrence of emissions of
        //another Observable will determine when to "slice" each buffer
        Flowable<Long> cutOffs =
                Flowable.interval(1, TimeUnit.SECONDS);
        Flowable.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .buffer(cutOffs)
                .subscribe(System.out::println);
        sleep(5000);
    }


    /*************************************windowing*****************************************************/

    @Test
    public void window() {
        //The window() operators are almost identical to buffer(), except that they buffer into other Observables rather
        //than collections. This results in an Observable<Observable<T>> that emits Observables. Each Observable emission
        //will cache emissions for each scope and then flush them once subscribed
        Flowable.range(1, 50).window(8)
                .flatMapSingle(flowable -> flowable.reduce("", (total, next) -> total + (total.equals("") ? "" : "|") + next))
                .subscribe(System.out::println);
    }

    @Test
    public void window1() {
        //Just like buffer(), you can also provide a skip argument. This is how many emissions need to be skipped
        //before starting a new window
        Flowable.range(1, 50).window(2, 3)
                .flatMapSingle(flowable -> flowable.reduce("", (total, next) -> total + (total.equals("") ? "" : "|") + next))
                .subscribe(System.out::println);
    }

    @Test
    public void window2() {
        //you can cut-off windowed Observables at time intervals just like buffer().
        //Here, we have an Observable emitting every 300 milliseconds like earlier, and we are slicing it into
        //separate Observables every 1 second
        Flowable.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .window(1, TimeUnit.SECONDS)
                .flatMapSingle(obs -> obs.reduce("", (total, next) -> total
                        + (total.equals("") ? "" : "|") + next))
                .subscribe(System.out::println);
        sleep(5000);
    }


    /*************************************Throttling*****************************************************/

    //The buffer() and window() operators batch up emissions into collections or Observables based on a defined
    //scope, which regularly consolidates rather than omits emissions.The throttle() operator, however, omits
    //emissions when they occur rapidly. This is helpful when rapid emissions are assumed to be redundant or
    //unwanted, such as a user clicking on a button repeatedly. For these situations, you can use the
    //throttleLast(), throttleFirst(), and throttleWithTimeout() operators to only let the first or last element in a rapid
    //sequence of emissions through. How you choose one of the many rapid emissions is determined by your
    //choice of operator, parameters, and arguments
    @Test
    public void throttleLast() {
        Flowable<String> source1 = Flowable.interval(100, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 100) // map to elapsed time
                .map(i -> "SOURCE 1: " + i)
                .take(10);

        source1.throttleLast(100, TimeUnit.MILLISECONDS).subscribe(System.out::println);

        sleep(60000);
    }

    @Test
    public void throttleLast1() {
        Flowable<String> source1 = Flowable.interval(100, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 100) // map to elapsed time
                .map(i -> "SOURCE 1: " + i)
                .take(10);
        Flowable<String> source2 = Flowable.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .map(i -> "SOURCE 2: " + i)
                .take(3);
        Flowable<String> source3 = Flowable.interval(2000, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 2000) // map to elapsed time
                .map(i -> "SOURCE 3: " + i)
                .take(2);

        Flowable.concat(source1, source2, source3)
                .throttleLast(1, TimeUnit.SECONDS)
                .subscribe(System.out::println);

        sleep(6000);
    }

    @Test
    public void throttleFirst() {
        //but it will emit the first item that occurs at
        //every fixed time interval
        Flowable<String> source1 = Flowable.interval(100, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 100) // map to elapsed time
                .map(i -> "SOURCE 1: " + i)
                .take(10);
        Flowable<String> source2 = Flowable.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .map(i -> "SOURCE 2: " + i)
                .take(3);
        Flowable<String> source3 = Flowable.interval(2000, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 2000) // map to elapsed time
                .map(i -> "SOURCE 3: " + i)
                .take(2);

        Flowable.concat(source1, source2, source3)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(System.out::println);

        sleep(60000);
    }

    @Test
    public void throttleWithTimeout() {
        Flowable<String> source1 = Flowable.interval(100, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 100) // map to elapsed time
                .map(i -> "SOURCE 1: " + i)
                .take(10);
        Flowable<String> source2 = Flowable.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .map(i -> "SOURCE 2: " + i)
                .take(3);
        Flowable<String> source3 = Flowable.interval(2000, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 2000) // map to elapsed time
                .map(i -> "SOURCE 3: " + i)
                .take(2);

        Flowable.concat(source1, source2, source3)
                .throttleWithTimeout(1, TimeUnit.SECONDS)
                .subscribe(System.out::println);

        sleep(60000);
    }

    @Test
    public void throttleLast2() {
        //If you want to throttle more liberally at larger time intervals, you will get fewer emissions as this
        //effectively reduces the sample frequency
        Flowable<String> source1 = Flowable.interval(100, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 100) // map to elapsed time
                .map(i -> "SOURCE 1: " + i)
                .take(10);
        Flowable<String> source2 = Flowable.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .map(i -> "SOURCE 2: " + i)
                .take(3);
        Flowable<String> source3 = Flowable.interval(2000, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 2000) // map to elapsed time
                .map(i -> "SOURCE 3: " + i)
                .take(2);

        Flowable.concat(source1, source2, source3)
                .throttleLast(2, TimeUnit.SECONDS)
                .subscribe(System.out::println);

        sleep(6000);
    }

    @Test
    public void throttleLast3() {
        //If you want to throttle more aggressively at shorter time intervals, you will get more emissions, as this
        //increases the sample frequency
        Flowable<String> source1 = Flowable.interval(100, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 100) // map to elapsed time
                .map(i -> "SOURCE 1: " + i)
                .take(10);
        Flowable<String> source2 = Flowable.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .map(i -> "SOURCE 2: " + i)
                .take(3);
        Flowable<String> source3 = Flowable.interval(2000, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 2000) // map to elapsed time
                .map(i -> "SOURCE 3: " + i)
                .take(2);

        Flowable.concat(source1, source2, source3)
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .subscribe(System.out::println);

        sleep(6000);
    }


    /*************************************Combining*****************************************************/

    /*************************************Merging*****************************************************/


    /*************************************Concatenation*****************************************************/

    @Test
    public void concat() {
        //The Observable.concat() factory is the concatenation equivalent to Observable.merge(). It will combine the
        //emissions of multiple Observables, but will fire each one sequentially and only move to the next after
        //onComplete() is called
        //This is the same output as our Observable.merge() example earlier. But as discussed in the merging section,
        //we should use Observable.concat() to guarantee emission ordering, as merging does not guarantee it
        Flowable<String> source1 =
                Flowable.just("Alpha", "Beta", "Gamma", "Delta",
                        "Epsilon");
        Flowable<String> source2 =
                Flowable.just("Zeta", "Eta", "Theta");
        Flowable.concat(source1, source2)
                .subscribe(i -> System.out.println("RECEIVED: " + i));
    }

    @Test
    public void concatWith() {
        Flowable<String> source1 =
                Flowable.just("Alpha", "Beta", "Gamma", "Delta",
                        "Epsilon");
        Flowable<String> source2 =
                Flowable.just("Zeta", "Eta", "Theta");
        source1.concatWith(source2).subscribe(i -> System.out.println("RECEIVED: " + i));
    }

    @Test
    public void concat2() {
        //emit every second, but only take 2 emissions
        Flowable<String> source1 =
                Flowable.interval(1, TimeUnit.SECONDS)
                        .take(2)
                        .map(l -> l + 1) // emit elapsed seconds
                        .map(l -> "Source1: " + l + " seconds");
//emit every 300 milliseconds
        Flowable<String> source2 =
                Flowable.interval(300, TimeUnit.MILLISECONDS)
                        .map(l -> (l + 1) * 300) // emit elapsed milliseconds
                        .map(l -> "Source2: " + l + " milliseconds");
        Flowable.concat(source1, source2)
                .subscribe(i -> System.out.println("RECEIVED: " + i));
//keep application alive for 5 seconds
        sleep(5000);
    }


    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void fileReadByBufferedReader() {
        String filePath = "/test.txt";

        Flowable.generate(() -> new BufferedReader(new InputStreamReader(FlowableTest.class.getResourceAsStream(filePath))), (reader, emitter) -> {
            String line = reader.readLine();
            if (line != null) {
                emitter.onNext(line);
            } else {
                emitter.onComplete();
            }
        }, reader -> reader.close()).observeOn(Schedulers.io()).subscribe(System.out::println, throwable -> throwable.printStackTrace(), () -> System.out.println("read complete"));
    }

    private static final int BUFFER_SIZE = 50;

    @Test
    public void fileRead() throws URISyntaxException, InterruptedException {
        String filePath = "/test.txt";
        Flowable.create(emitter -> {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(Paths.get(FlowableTest.class.getResource(filePath).toURI()), StandardOpenOption.READ);
            ByteBuffer dst = ByteBuffer.allocate(BUFFER_SIZE);
            fileChannel.read(dst, 0, dst, new FileReadHandler(fileChannel, emitter));
        }, BackpressureStrategy.BUFFER).subscribe(System.out::println, throwable -> throwable.printStackTrace(), () -> System.out.println("read complete"));

        TimeUnit.SECONDS.sleep(10);
    }

    private static class FileReadHandler implements CompletionHandler<Integer, ByteBuffer> {

        private AsynchronousFileChannel fileChannel;
        private Emitter emitter;

        //UTF-8解码
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = CharBuffer.allocate(BUFFER_SIZE);

        private long position;

        public FileReadHandler(AsynchronousFileChannel fileChannel, Emitter emitter) {
            this.fileChannel = fileChannel;
            this.emitter = emitter;
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {

            if (result < 0) {
                //读取结束了
                emitter.onComplete();
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return;
            }

            buffer.flip();

            decoder.decode(buffer, charBuffer, true);
            charBuffer.flip();

            char[] data = new char[charBuffer.limit()];
            charBuffer.get(data);
            String readInfo = new String(data);

            //发送获取的数据
            emitter.onNext(readInfo);

            charBuffer.clear();
            buffer.clear();

            //位置增加
            position += result;

            fileChannel.read(buffer, position, buffer, this);

        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            exc.printStackTrace();
        }

    }

    @Test
    public void firstElement() {
        //firstElement() operator, which is similar to first(), but it returns an empty result if no elements are emitted
        Flowable.just(1, 2, 3).firstElement().subscribe(System.out::println);
        Flowable.empty().firstElement().subscribe(i -> System.out.println(i), throwable -> throwable.printStackTrace(), () -> System.out.println("complete"));
    }
}
