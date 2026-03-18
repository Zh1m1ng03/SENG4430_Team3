package com.seng4430.qualitytesttool.dynamicanalysis.service;


import com.seng4430.qualitytesttool.dynamicanalysis.cofig.HttpConfig;
import com.seng4430.qualitytesttool.dynamicanalysis.cofig.ThreadPoolConfig;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ThroughPutTestService {


    HttpConfig httpConfig = new HttpConfig();
    OkHttpClient okHttpClient = httpConfig.getOkHttpClient();


    ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig();
    ThreadPoolExecutor threadPoolExecutor = threadPoolConfig.getThreadPool();





    private static final String BASE_URL = "http://localhost:9000";
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");





    // test get request throughput method
    private void runGetThroughput(String path, String testName) {
        String url = BASE_URL + path;

        int durationSeconds = 3;
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

        int durationSeconds = 3;
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

        int durationSeconds = 3;
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

    // post /register (multipart form with file upload)
    public void testRegisterThroughput() {
        String url = BASE_URL + "/register";
        String testName = "post /register";

        int durationSeconds = 3;
        int concurrency = 20;

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        long loopTime = System.currentTimeMillis() + durationSeconds * 1000L;

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < concurrency; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                int reqCount = 0;
                int n =0;
                while (System.currentTimeMillis() < loopTime) {
                    // generate unique user"
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("name", "Test User"+n)
                            .addFormDataPart("email", "testuser"+ n + "@test.com")
                            .addFormDataPart("pancardno", "ABCDE123"+n+"F")
                            .addFormDataPart("phone", "1234567890")
                            .addFormDataPart("password", "Test@1234"+n)
                            .addFormDataPart("dob", "2000-01-01")
                            .addFormDataPart("profile", "test.png",
                                    RequestBody.create(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}, MediaType.get("image/png")))
                            .build();
                    n++;

                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
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
                    reqCount++;
                }
            }, threadPoolExecutor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        printResult(testName, startTime, successCount);
    }

    // get /register page
    public void testRegisterPageThroughput() {
        runGetThroughput("/register", "get /register");
    }


    // get /login page
    public void testLoginPageThroughput() {
        runGetThroughput("/login", "get /login");
    }

    // post /login
    public void testLoginThroughput() {
        FormBody formBody = new FormBody.Builder()
                .add("email", "testuser0@test.com")
                .add("password", "Test@12340")
                .build();
        runPostFormThroughput("/login", formBody, "post /login");
    }




    // get homepage
    public void testHomeThroughput() {
        runGetThroughput("/", "get /");
    }




    // post /addMoney
    public void testAddMoneyThroughput() {
        FormBody formBody = new FormBody.Builder()
                .add("userId", "2")
                .add("amount", "100.0")
                .build();
        runPostFormThroughput("/addMoney", formBody, "post /addMoney");
    }

    // get /logout
    public void testLogoutThroughput() {
        runGetThroughput("/logout", "get /logout");
    }

    // get /profile
    public void testProfileThroughput() {
        runGetThroughput("/profile", "get /profile");
    }

    // get /profilePicture
    public void testGetProfilePictureThroughput() {
        runGetThroughput("/profilePicture?userId=2", "get /profilePicture");
    }

    // get /chatbot
    public void testChatbotPageThroughput() {
        runGetThroughput("/chatbot", "get /chatbot");
    }

    // get /liveMarket
    public void testLiveMarketThroughput() {
        runGetThroughput("/liveMarket", "get /liveMarket");
    }

    // get /learnToTrade
    public void testLearnToTradeThroughput() {
        runGetThroughput("/learnToTrade", "get /learnToTrade");
    }


    // get /ordersuccess
    public void testOrderSuccessThroughput() {
        runGetThroughput("/ordersuccess", "get /ordersuccess");
    }

    // post /StockTransaction
    public void testStockTransactionThroughput() {
        FormBody formBody = new FormBody.Builder()
                .add("transactionType", "buy")
                .add("userid", "2")
                .add("stockId", "1")
                .add("quantity", "10")
                .add("price", "150.0")
                .build();
        runPostFormThroughput("/StockTransaction", formBody, "post /StockTransaction");
    }

    // get /transactions
    public void testGetTransactionsThroughput() {
        FormBody formBody = new FormBody.Builder()
                .add("userId", "2")
                .build();
        runPostFormThroughput("/transactions", formBody, "post /transactions");
    }

    // get /trendingstocks
    public void testTrendingStocksThroughput() {
        runGetThroughput("/trendingstocks", "get /trendingstocks");
    }



    // get /portfolio
    public void testPortfolioThroughput() {
        runGetThroughput("/portfolio", "get /portfolio");
    }

    // get /api/portfolio
    public void testApiPortfolioThroughput() {
        runGetThroughput("/api/portfolio", "get /api/portfolio");
    }

    // get /getStockDetailsByCategory
    public void testGetStockDetailsByCategoryThroughput() {
        runGetThroughput("/getStockDetailsByCategory?category=Large Cap", "get /getStockDetailsByCategory");
    }

    // post /updateStopLossAndStopGain
    public void testUpdateStopLossAndStopGainThroughput() {
        FormBody formBody = new FormBody.Builder()
                .add("stockId", "1")
                .add("stopLoss", "100.00")
                .add("stopGain", "200.00")
                .build();
        runPostFormThroughput("/updateStopLossAndStopGain", formBody, "post /updateStopLossAndStopGain");
    }


    // get /nominee/list
    public void testNomineeListThroughput() {
        runGetThroughput("/nominee/list", "get /nominee/list");
    }

    // post /nominee/add
    public void testNomineeAddThroughput() {
        FormBody formBody = new FormBody.Builder()
                .add("nomineeName", "TestNominee")
                .add("phoneNo", "1234567890")
                .build();
        runPostFormThroughput("/nominee/add", formBody, "post /nominee/add");
    }

    // post /nominee/update
    public void testNomineeUpdateThroughput() {
        FormBody formBody = new FormBody.Builder()
                .add("nomineeId", "1")
                .add("nomineeName", "UpdatedNominee")
                .add("relationship", "Sister")
                .add("phoneNo", "0987654321")
                .build();
        runPostFormThroughput("/nominee/update", formBody, "post /nominee/update");
    }

    // post /nominee/delete
    public void testNomineeDeleteThroughput() {
        FormBody formBody = new FormBody.Builder()
                .add("nomineeId", "1")
                .build();
        runPostFormThroughput("/nominee/delete", formBody, "post /nominee/delete");
    }


    // post /api/chat
    public void testChatThroughput() {
        String jsonBody = "{\"message\": \"What is stock trading?\"}";
        runPostThroughput("/api/chat", jsonBody, "post /api/chat");
    }

    // get /chat
    public void testChatPageThroughput() {
        runGetThroughput("/chat", "get /chat");
    }


    // get /stocks
    public void testStocksThroughput() {
        runGetThroughput("/stocks?page=1&itemsPerPage=10&filterCategory=All&sortOrder=asc", "get /stocks");
    }

    // get /stockDetails
    public void testStockDetailsThroughput() {
        runGetThroughput("/stockDetails?stockId=1", "get /stockDetails");
    }

    // get /realtimestockdata
    public void testRealtimeStockDataThroughput() {
        runGetThroughput("/realtimestockdata", "get /realtimestockdata");
    }


    public void testAllThroughput() {
        System.out.println("---------------------------------------------------");
        System.out.println("   start throughput test");


        testRegisterThroughput();
        testRegisterPageThroughput();
        testLoginThroughput();
        testLoginPageThroughput();
        testNSEThroughput();
        testHomeThroughput();
        testAddMoneyThroughput();
        testProfileThroughput();
        testGetProfilePictureThroughput();
        testChatbotPageThroughput();
        testLiveMarketThroughput();
        testLearnToTradeThroughput();
        testOrderSuccessThroughput();
        testStockTransactionThroughput();
        testGetTransactionsThroughput();
        testTrendingStocksThroughput();
        testPortfolioThroughput();
        testApiPortfolioThroughput();
        testGetStockDetailsByCategoryThroughput();
        testUpdateStopLossAndStopGainThroughput();
        testNomineeListThroughput();
        testNomineeAddThroughput();
        testNomineeUpdateThroughput();
        testNomineeDeleteThroughput();
        testChatThroughput();
        testChatPageThroughput();
        testStocksThroughput();
        testStockDetailsThroughput();
        testRealtimeStockDataThroughput();
        testLogoutThroughput();

        System.out.println("---------------------------------------------------");
        System.out.println("   all throughput tests completed");

    }




}