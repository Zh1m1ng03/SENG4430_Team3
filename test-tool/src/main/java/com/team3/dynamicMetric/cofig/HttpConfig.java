package com.team3.dynamicMetric.cofig;

import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class HttpConfig {


    public OkHttpClient getOkHttpClient(){

        // okhttp config
        OkHttpClient okHttpClient= new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        return okHttpClient;

    }

}
