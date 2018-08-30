package com.taobao;

import io.reactivex.Observable;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/8/29 上午6:54
 */
public class SomeType {

    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    public Observable<String> valueObservable() {
        return Observable.just(value);
    }

    public Observable<String> deferObservable() {
        return Observable.defer(() -> Observable.just(value));
    }

    public Observable<String> coldObservable() {
        return Observable.create(emitter -> {
            emitter.onNext(value);
            emitter.onComplete();
        });
    }
}
