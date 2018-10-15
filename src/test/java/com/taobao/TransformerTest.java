package com.taobao;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import org.junit.Test;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/29 上午9:48
 */
public class TransformerTest {

    @Test
    public void test() {
        Observable.just(1, 2, 3).compose(transformer()).subscribe(System.out::println);
    }

    private ObservableTransformer<Integer, String> transformer() {
        return (upstream) -> upstream.map(integer -> "transform:" + String.valueOf(integer));
    }
}
