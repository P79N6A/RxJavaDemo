package com.taobao;

import io.reactivex.Observable;
import org.junit.Test;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/8/30 上午6:28
 */
public class RangeTest {

    @Test
    public void test() {
        Observable<Integer> observable = Observable.range(0, 100);
        observable.subscribe(System.out::println);
    }
}
