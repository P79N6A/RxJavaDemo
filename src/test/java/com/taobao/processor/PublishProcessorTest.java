package com.taobao.processor;

import io.reactivex.processors.PublishProcessor;
import org.junit.Test;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/12/14 上午11:00
 */
public class PublishProcessorTest {

    @Test
    public void test() {

        //PublishProcessor doesn't retain/cache items, therefore, a new Subscriber won't receive any past items

        PublishProcessor<String> processor = PublishProcessor.create();
        // subscriber1 will receive all onNext and onComplete events
        processor.subscribe(s -> System.out.println("Receive1:" + s));
        processor.onNext("one");
        processor.onNext("two");
        // subscriber2 will only receive "three" and onComplete
        processor.subscribe(s -> System.out.println("Receive2:" + s));
        processor.onNext("three");
        processor.onComplete();
    }
}
