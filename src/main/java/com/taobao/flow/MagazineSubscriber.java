package com.taobao.flow;

import java.util.concurrent.Flow;
import java.util.stream.IntStream;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/9/27 下午5:11
 */
public class MagazineSubscriber implements Flow.Subscriber<Integer> {

    public static final String JACK = "Jack";
    public static final String PETE = "Pete";

    private final long sleepTime;
    private final String subscriberName;
    private Flow.Subscription subscription;
    private int nextMagazineExpected;
    private int totalRead;

    public MagazineSubscriber(final long sleepTime, final String subscriberName) {
        this.sleepTime = sleepTime;
        this.subscriberName = subscriberName;
        this.nextMagazineExpected = 1;
        this.totalRead = 0;
    }


    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Integer magazineNumber) {
        //当publisher出现丢弃数据的时记录下来
        if (magazineNumber != nextMagazineExpected) {
            IntStream.range(nextMagazineExpected, magazineNumber).forEach(
                    (msgNumber) ->
                            log("Oh no! I missed the magazine " + msgNumber)
            );
            // Catch up with the number to keep tracking missing ones
            nextMagazineExpected = magazineNumber;
        }

        log("Great! I got a new magazine: " + magazineNumber);
        takeSomeRest();
        nextMagazineExpected++;
        totalRead++;

        log("I'll get another magazine now, next one should be: " +
                nextMagazineExpected);
        subscription.request(1);

    }

    private void takeSomeRest() {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onError(Throwable throwable) {
        log("Oops I got an error from the Publisher: " + throwable.getMessage());
    }

    @Override
    public void onComplete() {
        log("Finally! I completed the subscription, I got in total " +
                totalRead + " magazines.");
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    private void log(final String logMessage) {
        System.out.println("<=========== [" + subscriberName + "] : " + logMessage);
    }
}
