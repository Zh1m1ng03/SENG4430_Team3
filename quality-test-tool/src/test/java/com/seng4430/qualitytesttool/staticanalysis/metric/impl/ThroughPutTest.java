package com.seng4430.qualitytesttool.staticanalysis.metric.impl;

import com.seng4430.qualitytesttool.dynamicanalysis.service.ThroughPutTestService;
import org.junit.jupiter.api.Test;

public class ThroughPutTest {

    private ThroughPutTestService throughPutTestService= new ThroughPutTestService();

    @Test
    public void test(){



        throughPutTestService.testLoginThroughput();
        throughPutTestService.testProfileThroughput();

    }

}
