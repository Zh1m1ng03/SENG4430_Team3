package com.team3.dynamicMetric.service;

import com.team3.dynamicMetric.cofig.HttpConfig;
import com.team3.dynamicMetric.cofig.ThreadPoolConfig;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RunTimeTestService {


    HttpConfig httpConfig = new HttpConfig();
    OkHttpClient okHttpClient = httpConfig.getOkHttpClient();


    ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig();
    ThreadPoolExecutor threadPoolExecutor = threadPoolConfig.getThreadPool();





    private static final String BASE_URL = "http://localhost:9000";
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");





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

    // test post request and the parameter is @RequestBody
    private void runPostThroughput(String path, String jsonBody, String testName) {
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
                    RequestBody body = RequestBody.create(jsonBody, JSON_TYPE);
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
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


    // test post request and the parameter is @RequestParam (form-encoded)
    private void runPostFormThroughput(String path, FormBody formBody, String testName) {
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
                            .post(formBody)
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
        double qps = successCount.get() / (totalTimeMs / 1000.0); // convert to second
        System.out.println("----------" + testName + " Throughput Test ---------");
        System.out.println("Throughput(QPS): " + String.format("%.2f", qps));
        System.out.println("-----------------------------------------------------");

    }

    // Test method

    // get /market/nse
    public void testNSEThroughput() {
        runGetThroughput("/market/nse", "get /market/nse");
    }

    // ==================== UserController ====================

    // get homepage
    public void testHomeThroughput() {
        runGetThroughput("/", "GET /");
    }

    // get /login
    public void testLoginPageThroughput() {
        runGetThroughput("/login", "GET /login");
    }

    // post /login
    public void testLoginThroughput() {
        FormBody formBody = new FormBody.Builder()
                .add("email", "test@example.com")
                .add("password", "Test@1234")
                .build();
        runPostFormThroughput("/login", formBody, "POST /login");
    }

    // get /register
    public void testRegisterPageThroughput() {
        runGetThroughput("/register", "GET /register");
    }

    // post /addMoney
    public void testAddMoneyThroughput() {
        FormBody formBody = new FormBody.Builder()
                .add("userId", "1")
                .add("amount", "100.0")
                .build();
        runPostFormThroughput("/addMoney", formBody, "POST /addMoney");
    }

    // get /logout
    public void testLogoutThroughput() {
        runGetThroughput("/logout", "GET /logout");
    }

    // get /profile
    public void testProfileThroughput() {
        runGetThroughput("/profile", "GET /profile");
    }

    // get /profilePicture
    public void testGetProfilePictureThroughput() {
        runGetThroughput("/profilePicture?userId=1", "GET /profilePicture");
    }


}