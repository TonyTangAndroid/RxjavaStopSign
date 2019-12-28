package com.tonytangandroid.rxjava_stop_sign.demo;

import com.jakewharton.rxrelay2.BehaviorRelay;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;

import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.BDDMockito.given;

public class ThrottleCarStreamTest {

    private TestObserver<Car> testObserver;

    //Car id starting from zero.
    private BehaviorRelay<Car> relay = BehaviorRelay.createDefault(Car.builder().id("0").build());

    @Mock private CarStream mockCarStream;
    
    private TestScheduler scheduler = new TestScheduler();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        given(mockCarStream.cmdStream()).willReturn(relay);
        ThrottleCarStream periodUserActionStream = new ThrottleCarStream(mockCarStream, scheduler);
        testObserver = periodUserActionStream.periodUserActionStream().test();
    }


    @Test
    public void first_car_should_be_passed_without_delay() {
        scheduler.advanceTimeBy(0, MILLISECONDS);
        testObserver.assertValueCount(1);
    }

    @Test
    public void when_second_car_comes_1_seconds_later_should_only_wait_1500_milliseconds() {
        //given
        scheduler.advanceTimeBy(1000, MILLISECONDS);
        //when the second car appears. It shall wait.
        relay.accept(Car.builder().id("1").build());
        //then we still only have 1 car passed 
        testObserver.assertValueCount(1);

        //when the time passes another 1500 milliseconds
        scheduler.advanceTimeBy(1500, MILLISECONDS);
        
        //then the second car will be passed.
        testObserver.assertValueCount(2);
    }


    @Test
    public void four_seconds_only_allows_two_car_passe_when_three_cars_show_up_all_together() {

        //Here comes 3 cars
        relay.accept(Car.builder().id("1").build());
        relay.accept(Car.builder().id("2").build());
        relay.accept(Car.builder().id("3").build());

        scheduler.advanceTimeBy(1000, MILLISECONDS);
        testObserver.assertValueCount(1);


        scheduler.advanceTimeBy(999, MILLISECONDS);
        testObserver.assertValueCount(1);

        scheduler.advanceTimeBy(501, MILLISECONDS);
        testObserver.assertValueCount(2);

        scheduler.advanceTimeBy(2499, MILLISECONDS);
        testObserver.assertValueCount(2);
    }


    @Test
    public void all_4_cars_pass_when_4_cars_show_up_all_together_after_7500_milliseconds() {
        relay.accept(Car.builder().id("1").build());
        relay.accept(Car.builder().id("2").build());
        relay.accept(Car.builder().id("3").build());
        scheduler.advanceTimeBy(7500, MILLISECONDS);
        testObserver.assertValueCount(4);
    }

    @Test
    public void only_pass_4_car_when_5_cars_show_up_all_together_after_7500_milliseconds() {
        relay.accept(Car.builder().id("1").build());
        relay.accept(Car.builder().id("2").build());
        relay.accept(Car.builder().id("3").build());
        relay.accept(Car.builder().id("4").build());
        scheduler.advanceTimeBy(7500, MILLISECONDS);
        testObserver.assertValueCount(4);
    }

    @Test
    public void the_second_car_will_pass_instantly_after_the_first_car_passes_40_seconds_ago() {
        scheduler.advanceTimeBy(40000, MILLISECONDS);
        testObserver.assertValueCount(1);

        relay.accept(Car.builder().id("1").build());

        scheduler.advanceTimeBy(0, MILLISECONDS);
        testObserver.assertValueCount(2);
    }


    @Test
    public void now_comes_a_complicated_use_case() {

        //First car should be passed instantly without waiting.
        scheduler.advanceTimeBy(0, MILLISECONDS);
        testObserver.assertValueCount(1);

        //Ten seconds passed, because no new car shows up.
        scheduler.advanceTimeBy(10000, MILLISECONDS);
        //So still we only have 1 car passed
        testObserver.assertValueCount(1);

        //Suddenly, we have 7 new car show up.
        relay.accept(Car.builder().id("1").build());
        relay.accept(Car.builder().id("2").build());
        relay.accept(Car.builder().id("3").build());
        relay.accept(Car.builder().id("4").build());
        relay.accept(Car.builder().id("5").build());
        relay.accept(Car.builder().id("6").build());
        relay.accept(Car.builder().id("7").build());

        //The second car passes instantly because the first car passes 10 seconds ago.
        testObserver.assertValueCount(2);

        //Move time to 1999 milliseconds, the third car is still waiting
        scheduler.advanceTimeBy(1999, MILLISECONDS);
        testObserver.assertValueCount(2);

        //Move time to 501 milliseconds
        scheduler.advanceTimeBy(501, MILLISECONDS);
        //the third car passes
        testObserver.assertValueCount(3);

        //Move time to 2500 milliseconds
        scheduler.advanceTimeBy(2500, MILLISECONDS);
        //The fourth car passes
        testObserver.assertValueCount(4);

        //Move time to 5000 milliseconds
        scheduler.advanceTimeBy(5000, MILLISECONDS);
        //The fifth and sixth car pass
        testObserver.assertValueCount(6);

        //Move time to 5000 milliseconds
        scheduler.advanceTimeBy(5000, MILLISECONDS);
        //The seventh and eighth car pass
        testObserver.assertValueCount(8);

        //Move one more hour
        scheduler.advanceTimeBy(1, TimeUnit.HOURS);
        //No new car passes because all car are passed.
        testObserver.assertValueCount(8);

        //Now here comes a new car.
        relay.accept(Car.builder().id("9").build());
        //the new car should be passed instantly because the last car passes an hour ago.
        testObserver.assertValueCount(9);

        //Now here comes another new car.
        relay.accept(Car.builder().id("10").build());
        //It has to wait.
        testObserver.assertValueCount(9);
        //The waiting time is over.
        scheduler.advanceTimeBy(2500, MILLISECONDS);
        //The ten car passes.
        testObserver.assertValueCount(10);
    }

}