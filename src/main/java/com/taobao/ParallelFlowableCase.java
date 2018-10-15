package com.taobao;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/9 下午8:05
 */
public class ParallelFlowableCase {

    public static void main(String[] args) {
        final long userId = 123L;

        List<Single<ResultEntity>> singleListList = new ArrayList<>();

        //服务编排

        singleListList.add(getItemCount(userId));
        singleListList.add(getShopCount(userId));
        singleListList.add(getFeedCount(userId));

        disorderCase(singleListList);

    }

    private static void disorderCase(List<Single<ResultEntity>> singleListList) {
        Single.concat(singleListList).doOnNext(resultEntity -> System.out.println(Thread.currentThread().getName() + "\t" + "Key:" + resultEntity.getKey() + "Value:" + resultEntity.getEntity())).blockingSubscribe();
    }

    /**
     * 模拟hsf异步化调用
     */

    private static Single<ResultEntity> getShopCount(long userId) {
        return Single.just(userId).observeOn(Schedulers.io()).map(s -> {
            ResultEntity resultEntity = new ResultEntity();
            resultEntity.setKey("shopCount");
            resultEntity.setEntity(10);
            return resultEntity;
        }).delay(2, TimeUnit.SECONDS, Schedulers.io());
    }

    /**
     * 模拟hsf异步化调用
     */

    private static Single<ResultEntity> getItemCount(long userId) {
        return Single.just(userId).observeOn(Schedulers.io()).map(s -> {
            ResultEntity resultEntity = new ResultEntity();
            resultEntity.setKey("itemCount");
            resultEntity.setEntity(100);
            return resultEntity;
        }).delay(2, TimeUnit.SECONDS, Schedulers.io());
    }


    private static Single<ResultEntity> getFeedCount(long userId) {
        return Single.just(userId).observeOn(Schedulers.io()).map(s -> {
            ResultEntity resultEntity = new ResultEntity();
            resultEntity.setKey("feedCount");
            resultEntity.setEntity(20);
            return resultEntity;
        }).delay(5, TimeUnit.SECONDS, Schedulers.io());
    }
}
