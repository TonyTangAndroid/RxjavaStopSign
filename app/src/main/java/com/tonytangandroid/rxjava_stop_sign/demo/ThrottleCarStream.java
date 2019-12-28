package com.tonytangandroid.rxjava_stop_sign.demo;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

public class ThrottleCarStream {

    private final CarStream carStream;
    private final Scheduler scheduler;
    private int passedCarCount;
    private int timeSpanCount;

    public ThrottleCarStream(CarStream carStream, Scheduler scheduler) {
        this.carStream = carStream;
        this.scheduler = scheduler;
    }


    public Observable<Car> periodUserActionStream() {
        return zip();
    }


    private Observable<Car> zip() {
        return Observable.zip(rawStream(), internal(), this::value);
    }

    private Observable<Long> internal() {
        return Observable.interval(0, 2500, TimeUnit.MILLISECONDS, scheduler)
            .doOnSubscribe(disposable -> initSpanCount())
            .filter(c -> whenCarCountGreaterOrEqualThanTimeSpanCount())
            .doOnNext(aLong -> increaseSpanCount());
    }

    private boolean whenCarCountGreaterOrEqualThanTimeSpanCount() {
        return passedCarCount >= timeSpanCount;
    }

    private void increaseSpanCount() {
        timeSpanCount++;
    }

    private void initSpanCount() {
        timeSpanCount = 0;
    }

    private Car value(Car cmdMessage, Long time) {
        return cmdMessage;
    }

    private Observable<Car> rawStream() {
        return carStream.cmdStream()
            .doOnSubscribe(disposable -> initElementCount())
            .doOnNext(cmdMessage -> increaseElementCount());
    }

    private void increaseElementCount() {
        passedCarCount++;
    }

    private void initElementCount() {
        passedCarCount = 0;
    }


}
