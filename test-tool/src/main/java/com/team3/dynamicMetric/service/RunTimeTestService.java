package com.team3.dynamicMetric.service;

import com.team3.config.OkHttpConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RunTimeTestService {


    @Autowired
    OkHttpClient okHttpClient;

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


}
