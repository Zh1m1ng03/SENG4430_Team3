package com.team3.dynamicMetric.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RunTimeTestService {

    @Autowired
    OkHttpClient okHttpClient;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    private static final String BASE_URL = "http://localhost:9000";
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");




     // get /market/nse test

    public void testNSEThroughput() {
        runGetThroughput("/market/nse", "market/nse");
    }


    // test get request throughput method

    private void runGetThroughput(String path, String testName) {
        String url = BASE_URL + path;

        int durationSeconds = 10;
        // 20 threads
        int concurrency = 20;

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        long loopTime = System.currentTimeMillis() + durationSeconds * 1000L; // loop time

        // store all asynchronous tasks
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis(); // start time

        for (int i = 0; i < concurrency; i++) {

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {

                while (System.currentTimeMillis() < loopTime) {

                    // send request
                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();

                    try (Response response = okHttpClient.newCall(request).execute()) {

                        if (response.isSuccessful()) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }

                    } catch (IOException e) {
                        errorCount.incrementAndGet();
                    }
                }

            }, threadPoolExecutor);

            futures.add(future);
        }

        // wait all concurrent finish
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        printResult(testName, startTime, successCount);
    }



    private void printResult(String testName, long startTime, AtomicInteger successCount) {
        long endTime = System.currentTimeMillis(); // endtime
        long totalTimeMs = endTime - startTime;
        double qps = successCount.get() / (totalTimeMs / 1000.0);
        System.out.println("----------" + testName + " Throughput Test ---------");
        System.out.println("Throughput(QPS): " + String.format("%.2f", qps));
        System.out.println("-----------------------------------------------------");

    }
}