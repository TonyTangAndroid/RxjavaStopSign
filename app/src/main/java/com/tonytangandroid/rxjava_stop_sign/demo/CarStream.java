package com.tonytangandroid.rxjava_stop_sign.demo;

import io.reactivex.Observable;

class CarStream {


    Observable<Car> cmdStream() {
        throw new RuntimeException("Will use mock car stream in test code");
    }
}
