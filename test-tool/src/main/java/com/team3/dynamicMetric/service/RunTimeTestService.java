package com.team3.dynamicMetric.service;

import com.team3.dynamicMetric.config.OkHttpConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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

    private static final String BASE_URL = "http://localhost:9000"; // target project url

    public void testNSE() {

        String url = BASE_URL + "/market/nse";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        long start = System.nanoTime(); // start time




        try (Response response = okHttpClient.newCall(request).execute() ){ // send request

            long end = System.nanoTime(); // end time

            long executeTime = (end - start) / 1_000_000; // convert to ms

            if(response.isSuccessful()){ // request success

                System.out.println("execute time: " + executeTime + " ms");

            }
            else {
                System.out.println("request failed: " + response.code());
            }





        } catch (IOException e) {
            System.err.println("request failed:" + e.getMessage());
        }
    }

    public void testNSEThroughput() {

        String url = BASE_URL + "/market/nse";

        int durationSeconds = 10;   // test time
        int concurrency = 20;       // concurrent tasks number

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        long loopTime = System.currentTimeMillis() + durationSeconds * 1000; // loop time

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < concurrency; i++) {

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {

                while (System.currentTimeMillis() < loopTime) {

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

        // wait all thread
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long endTime = System.currentTimeMillis();
        long totalTimeMs = endTime - startTime;

        double qps = successCount.get() / (totalTimeMs / 1000.0);
        System.out.println("Throughput is:  " + qps);

    }


}
