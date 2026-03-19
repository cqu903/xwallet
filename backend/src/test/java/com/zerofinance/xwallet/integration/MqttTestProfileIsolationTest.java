package com.zerofinance.xwallet.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
class MqttTestProfileIsolationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldNotStartMqttAdapterForTestProfile() {
        assertFalse(applicationContext.containsBean("mqttAdapter"));
    }
}
