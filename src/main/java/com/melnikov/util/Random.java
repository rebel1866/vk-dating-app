package com.melnikov.util;

public class Random {
    public static long getRandom(long start, long end) {
        return (long) (start + (Math.random() * (end - start + 1)));
    }
}
