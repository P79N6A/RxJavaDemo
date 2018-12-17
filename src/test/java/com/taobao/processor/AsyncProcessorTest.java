package com.taobao.processor;

import io.reactivex.processors.AsyncProcessor;
import org.junit.Test;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/12/14 上午11:09
 */
public class AsyncProcessorTest {

    @Test
    public void test() {

        AsyncProcessor<String> processor = AsyncProcessor.create();
        processor.onNext("1");
        // AsyncProcessor only emits when onComplete was called.
        processor.subscribe(s -> System.out.println("Receive1:" + s));
        processor.onNext("2");
        processor.onComplete();

        // late Subscribers receive the last cached item too
        processor.subscribe(s -> System.out.println("Receive2:" + s));
    }
}
