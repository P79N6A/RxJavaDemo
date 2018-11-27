package com.taobao.processor;

import io.reactivex.processors.ReplayProcessor;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/11/26 上午10:30
 */
public class ReplayProcessorTest {

    @Test
    public void create() {

        ReplayProcessor<Integer> replayProcessor = ReplayProcessor.create(1);


        IntStream.rangeClosed(1,5).forEach(e->{
            replayProcessor.onNext(e);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });

        replayProcessor.map(i->i+1).subscribe(System.out::println);

        replayProcessor.onComplete();
    }
}
