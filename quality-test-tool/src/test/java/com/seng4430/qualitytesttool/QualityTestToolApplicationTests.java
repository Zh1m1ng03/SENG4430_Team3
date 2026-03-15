package com.seng4430.qualitytesttool;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.cli.enabled=false")
class QualityTestToolApplicationTests {

    @Test
    void contextLoads() {
    }

}
