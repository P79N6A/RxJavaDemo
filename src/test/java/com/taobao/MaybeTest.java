package com.taobao;

import io.reactivex.*;
import org.testng.annotations.Test;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/30 下午2:06
 */
public class MaybeTest {

    @Test
    public void test() {
        //which represents a stream which can emit a single value, complete in an empty state or report an error
        subscribe(Maybe.just(1));
        //Maybe.empty().subscribe(…) will print “Completed. No items.”
        subscribe(Maybe.empty());
        //Maybe.error(new Exception(“error”)).subscribe(…) will print “Error: error”
        subscribe(Maybe.error(new Exception("error")));
    }

    private void subscribe(Maybe maybe) {
        maybe.subscribe(
                x -> System.out.println("Emitted item: " + x),
                ex -> System.out.println("Error: " + ((Throwable) ex).getMessage()),
                () -> System.out.println("Completed. No items.")
        );
    }

    @Test
    public void compose() {
        subscribe(Maybe.just(1).compose(filter()));
    }

    private MaybeTransformer<Integer, Integer> filter() {
        return upstream -> upstream.map(integer -> integer + 2);
    }

    @Test
    public void onSuccess() {
        Maybe.just(1).doOnSuccess(i -> System.out.println(i)).doOnComplete(() -> {
            System.out.println("Completed. No items.");
        }).subscribe();
    }

    @Test
    public void just() {
        Maybe.just(1).map(x -> x + 7).filter(x -> x > 0).test().assertResult(0);
    }

    @Test
    public void create() {

        Single<Object> single = Maybe.create(emitter -> {
            if (new Random().nextInt() % 2 == 0) {
                emitter.onSuccess(true);
            } else {
                emitter.onComplete();
            }
        }).toSingle(false);

        single.subscribe(o -> System.out.println(o));
    }

    /**
     * 当没有发送数据时执行switchIfEmpty操作
     */
    @Test
    public void switchIfEmpty() {
        Maybe.create(emitter -> {
            if (new Random().nextInt() % 2 == 0) {
                emitter.onSuccess(true);
            } else {
                emitter.onComplete();
            }
        }).switchIfEmpty(Maybe.just(false)).subscribe(o -> System.out.println(o));
    }

    @Test
    public void def() {
        Maybe.just(1).switchIfEmpty(callback()).blockingGet();
        Maybe.just(1).switchIfEmpty(Maybe.defer(this::callback)).blockingGet();
    }

    private Maybe<Integer> callback() {
        System.out.println("Fallback executed anyway");
        return Maybe.just(2);
    }
}
