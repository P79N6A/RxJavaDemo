package com.taobao.creating;

import io.reactivex.Observable;
import org.junit.Test;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/8/28 上午6:32
 */
public class JustTest {

    @Test
    public void test() {


        Observable<String> observable = Observable.just("1", "A", "3.2", "def");

        observable.subscribe(s -> System.out.println(s));
    }
}
