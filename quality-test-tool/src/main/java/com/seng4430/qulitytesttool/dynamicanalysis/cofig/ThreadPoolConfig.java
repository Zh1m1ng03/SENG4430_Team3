package com.seng4430.qulitytesttool.dynamicanalysis.cofig;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolConfig {


    public ThreadPoolExecutor getThreadPool(){


        // threadpool config
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor( 20,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        return threadPoolExecutor;

    }

}
