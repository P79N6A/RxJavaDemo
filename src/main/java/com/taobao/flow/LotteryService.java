package com.taobao.flow;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单抽奖程序模拟
 * 1、先增加抽奖次数（可能失败）
 * 2、如果抽奖成功扣除总库存（可能失败）
 * 3、如果扣除库存失败，将之前增加的抽奖次数减去
 * <p>
 * <p>
 * 核心点：Single,Maybe
 * 操作符：filter,switchIfEmpty
 *
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/12 上午11:26
 */
public class LotteryService {

    public static void main(String[] args) {
        LotteryService lotteryService = new LotteryService();
        lotteryService.draw();
    }

    private static final int DEFAULT_LOOP = 10;

    /**
     * 中奖次数
     */
    private AtomicInteger userLottery = new AtomicInteger(0);

    /**
     * 总库存
     */
    private AtomicInteger lotteryInventory = new AtomicInteger(DEFAULT_LOOP);

    /**
     * 抽奖逻辑
     */
    public void draw() {
        AtomicInteger sc = new AtomicInteger(0);
        AtomicInteger fc = new AtomicInteger(0);

        for (int i = 0; i < DEFAULT_LOOP; i++) {
            increase().filter(success -> success)//判断是否处理成功
                    .flatMapSingleElement(result -> reduceTotalLottery().filter(success -> success).switchIfEmpty(decrease()))
                    .doOnSuccess(success -> System.out.println(String.format("success:%s", sc.incrementAndGet())))
                    .doOnComplete(() -> System.out.println(String.format("fail:%s", fc.incrementAndGet())))
                    .doOnError(throwable -> throwable.printStackTrace()).blockingGet();
        }


        //注意这里的success次数和中奖次数没有关系，因为可能中奖了，但是库存扣除失败了，也是算处理成功的
        System.out.println(String.format("处理完毕，中奖次数：%s , 总库存：%s", userLottery.get(), lotteryInventory.get()));
    }


    /**
     * 模拟异步增加抽奖次数
     * <p>
     * 可能失败
     *
     * @return
     */
    private Single<Boolean> increase() {
        return Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            int rnd = new Random().nextInt(10);
            if (rnd % 2 == 0) {
                userLottery.incrementAndGet();
                emitter.onSuccess(true);
            } else {
                emitter.onSuccess(false);
            }
        }).subscribeOn(Schedulers.io()).delay(100, TimeUnit.MILLISECONDS, Schedulers.io());
    }

    /**
     * 模拟异步减少抽奖次数
     * <p>
     * 不会失败
     *
     * @return
     */
    private Single<Boolean> decrease() {
        return Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            userLottery.decrementAndGet();
            emitter.onSuccess(true);
        }).subscribeOn(Schedulers.io()).delay(100, TimeUnit.MILLISECONDS, Schedulers.io());
    }

    /**
     * 模拟异步减少总库存
     * <p>
     * 可能失败
     *
     * @return
     */
    private Single<Boolean> reduceTotalLottery() {
        return Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            int rnd = new Random().nextInt(100);
            if (rnd % 2 == 0) {
                lotteryInventory.decrementAndGet();
                emitter.onSuccess(true);
            } else {
                emitter.onSuccess(false);
            }
        }).subscribeOn(Schedulers.io()).delay(200, TimeUnit.MILLISECONDS, Schedulers.io());
    }


}
