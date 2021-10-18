package com.daggerok.spring.batch.util;

import lombok.SneakyThrows;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Sleep {

    private static Random random = new Random();

    private Sleep() {}

    public static void some() {
        seconds(random.nextInt(5));
    }

    @SneakyThrows
    public static void seconds(int seconds) {
        TimeUnit.SECONDS.sleep(seconds);
    }
}
