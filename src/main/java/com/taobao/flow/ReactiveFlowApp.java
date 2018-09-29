package com.taobao.flow;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/27 下午5:52
 */
public class ReactiveFlowApp {

    private static final int NUMBER_OF_MAGAZINES = 20;
    private static final long MAX_SECONDS_TO_KEEP_IT_WHEN_NO_SPACE = 2;

    public static void main(String[] args) throws Exception {
        final ReactiveFlowApp app = new ReactiveFlowApp();

        log("\n\n### CASE 1: Subscribers are fast, buffer size is not so " +
                "important in this case.");
        app.magazineDeliveryExample(100L, 100L, 8);

        log("\n\n### CASE 2: A slow subscriber, but a good enough buffer " +
                "size on the publisher's side to keep all items until they're picked up");
        app.magazineDeliveryExample(1000L, 3000L, NUMBER_OF_MAGAZINES);

        log("\n\n### CASE 3: A slow subscriber, and a very limited buffer " +
                "size on the publisher's side so it's important to keep the slow " +
                "subscriber under control");
        app.magazineDeliveryExample(1000L, 3000L, 8);

    }

    void magazineDeliveryExample(final long sleepTimeJack,
                                 final long sleepTimePete,
                                 final int maxStorageInPO) throws Exception {
        //指定最大缓存池大小
        final SubmissionPublisher<Integer> publisher =
                new SubmissionPublisher<>(ForkJoinPool.commonPool(), maxStorageInPO);

        //两个消费者,设置了等待时间
        final MagazineSubscriber jack = new MagazineSubscriber(
                sleepTimeJack,
                MagazineSubscriber.JACK
        );

        final MagazineSubscriber pete = new MagazineSubscriber(
                sleepTimePete,
                MagazineSubscriber.PETE
        );

        //指定订阅者
        publisher.subscribe(jack);
        publisher.subscribe(pete);

        log("Printing 20 magazines per subscriber, with room in publisher for "
                + maxStorageInPO + ". They have " + MAX_SECONDS_TO_KEEP_IT_WHEN_NO_SPACE +
                " seconds to consume each magazine.");

        //发送数据
        IntStream.rangeClosed(1, 20).forEach((number) -> {
            log("Offering magazine " + number + " to consumers");
            //每隔两秒发送一个数据
            final int lag = publisher.offer(
                    number,
                    MAX_SECONDS_TO_KEEP_IT_WHEN_NO_SPACE,
                    TimeUnit.SECONDS,
                    (subscriber, msg) -> {
                        subscriber.onError(
                                new RuntimeException("Hey " + ((MagazineSubscriber) subscriber).getSubscriberName() + "! You are too slow getting magazines" +
                                        " and we don't have more space for them! " +
                                        "I'll drop your magazine: " + msg));
                        return false; // don't retry, we don't believe in second opportunities
                    });
            if (lag < 0) {
                log("Dropping " + -lag + " magazines");
            } else {
                log("The slowest consumer has " + lag +
                        " magazines in total to be picked up");
            }
        });

        // Blocks until all subscribers are done (this part could be improved
        // with latches, but this way we keep it simple)
        while (publisher.estimateMaximumLag() > 0) {
            Thread.sleep(500L);
        }

        // Closes the publisher, calling the onComplete() method on every subscriber
        publisher.close();
        // give some time to the slowest consumer to wake up and notice
        // that it's completed
        Thread.sleep(Math.max(sleepTimeJack, sleepTimePete));
    }

    private static void log(final String message) {
        System.out.println("===========> " + message);
    }
}
