package com.seng4430.qualitytesttool.staticanalysis.metric.impl;

import com.seng4430.qualitytesttool.dynamicanalysis.service.ThroughPutTestService;
import org.junit.jupiter.api.Test;

public class ThroughPutTest {

    private ThroughPutTestService throughPutTestService = new ThroughPutTestService();

    @Test
    public void testRegisterThroughput() {
        throughPutTestService.testRegisterThroughput();
    }

    @Test
    public void testRegisterPageThroughput() {
        throughPutTestService.testRegisterPageThroughput();
    }

    @Test
    public void testLoginPageThroughput() {
        throughPutTestService.testLoginPageThroughput();
    }

    @Test
    public void testLoginThroughput() {
        throughPutTestService.testLoginThroughput();
    }

    @Test
    public void testNSEThroughput() {
        throughPutTestService.testNSEThroughput();
    }




    @Test
    public void testHomeThroughput() {
        throughPutTestService.testHomeThroughput();
    }

    @Test
    public void testAddMoneyThroughput() {
        throughPutTestService.testAddMoneyThroughput();
    }


    @Test
    public void testProfileThroughput() {
        throughPutTestService.testProfileThroughput();
    }

    @Test
    public void testGetProfilePictureThroughput() {
        throughPutTestService.testGetProfilePictureThroughput();
    }

    @Test
    public void testChatbotPageThroughput() {
        throughPutTestService.testChatbotPageThroughput();
    }

    @Test
    public void testLiveMarketThroughput() {
        throughPutTestService.testLiveMarketThroughput();
    }

    @Test
    public void testLearnToTradeThroughput() {
        throughPutTestService.testLearnToTradeThroughput();
    }

    @Test
    public void testOrderSuccessThroughput() {
        throughPutTestService.testOrderSuccessThroughput();
    }

    @Test
    public void testStockTransactionThroughput() {
        throughPutTestService.testStockTransactionThroughput();
    }

    @Test
    public void testGetTransactionsThroughput() {
        throughPutTestService.testGetTransactionsThroughput();
    }

    @Test
    public void testTrendingStocksThroughput() {
        throughPutTestService.testTrendingStocksThroughput();
    }

    @Test
    public void testPortfolioThroughput() {
        throughPutTestService.testPortfolioThroughput();
    }

    @Test
    public void testApiPortfolioThroughput() {
        throughPutTestService.testApiPortfolioThroughput();
    }

    @Test
    public void testGetStockDetailsByCategoryThroughput() {
        throughPutTestService.testGetStockDetailsByCategoryThroughput();
    }

    @Test
    public void testUpdateStopLossAndStopGainThroughput() {
        throughPutTestService.testUpdateStopLossAndStopGainThroughput();
    }

    @Test
    public void testNomineeListThroughput() {
        throughPutTestService.testNomineeListThroughput();
    }

    @Test
    public void testNomineeAddThroughput() {
        throughPutTestService.testNomineeAddThroughput();
    }

    @Test
    public void testNomineeUpdateThroughput() {
        throughPutTestService.testNomineeUpdateThroughput();
    }

    @Test
    public void testNomineeDeleteThroughput() {
        throughPutTestService.testNomineeDeleteThroughput();
    }

    @Test
    public void testChatThroughput() {
        throughPutTestService.testChatThroughput();
    }

    @Test
    public void testChatPageThroughput() {
        throughPutTestService.testChatPageThroughput();
    }

    @Test
    public void testStocksThroughput() {
        throughPutTestService.testStocksThroughput();
    }

    @Test
    public void testStockDetailsThroughput() {
        throughPutTestService.testStockDetailsThroughput();
    }

    @Test
    public void testRealtimeStockDataThroughput() {
        throughPutTestService.testRealtimeStockDataThroughput();
    }

    @Test
    public void testLogoutThroughput() {
        throughPutTestService.testLogoutThroughput();
    }


    @Test
    public void testAllThroughput() {
        throughPutTestService.testAllThroughput();
    }

}
