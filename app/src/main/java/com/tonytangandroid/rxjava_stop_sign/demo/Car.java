package com.tonytangandroid.rxjava_stop_sign.demo;

import com.google.auto.value.AutoValue;


@AutoValue
public abstract class Car {

    public abstract String id();

    public static Builder builder() {
        return new AutoValue_Car.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Car build();
    }
}