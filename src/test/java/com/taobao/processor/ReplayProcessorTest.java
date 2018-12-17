package com.taobao.processor;

import io.reactivex.Flowable;
import io.reactivex.processors.ReplayProcessor;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/11/26 上午10:30
 */
public class ReplayProcessorTest {

    @Test
    public void create() throws InterruptedException {


        ReplayProcessor<Long> replayProcessor = ReplayProcessor.createWithSize(1);

        Flowable.interval(100, TimeUnit.MILLISECONDS).doOnComplete(() -> replayProcessor.onComplete()).doOnNext(i -> {
            replayProcessor.onNext(i);
            replayProcessor.onNext(i + 100);
        }).subscribe();

        replayProcessor.map(i -> i + 1).subscribe(i -> {
            TimeUnit.SECONDS.sleep(1);
            System.out.println(i);
        });

        TimeUnit.SECONDS.sleep(5);


    }
}
