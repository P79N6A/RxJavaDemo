package com.taobao.conditional;

import io.reactivex.Observable;
import org.junit.Test;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/18 上午7:15
 */
public class ElementAtTest {

    @Test
    public void elementAt() {
        Observable observable = Observable.just(1, 2, 3, 4);
        observable.elementAt(1).subscribe(System.out::println);
    }

    @Test
    public void elementAtWithDefaultItem() {
        Observable observable = Observable.just(1, 2, 3, 4);
        observable.elementAt(4, 0).subscribe(System.out::println);
    }

    @Test
    public void elementAtOrError(){
        Observable observable = Observable.just(1, 2, 3, 4);
        observable.elementAtOrError(4).subscribe(System.out::println,throwable -> System.out.println(throwable));
    }
}
