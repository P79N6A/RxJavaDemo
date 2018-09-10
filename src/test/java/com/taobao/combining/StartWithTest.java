package com.taobao.combining;

import io.reactivex.Observable;
import org.junit.Test;

/**
 * Emit a specified sequence of items before beginning to emit the items from the Observable
 *
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/8/31 上午7:03
 */
public class StartWithTest {

    @Test
    public void test() {
        Observable<String> observable = Observable.just("Hello World");
        observable.startWith("Hi").subscribe(System.out::println);
    }
}
