package com.melnikov.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Component
public class ThreadPool {
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
