package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.AnalyticsEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("风控评估服务单元测试")
class RiskEvaluationServiceTest {

    private final RiskEvaluationService riskEvaluationService = new RiskEvaluationService();

    @Test
    @DisplayName("登录失败事件可正常评估")
    void testEvaluateLoginFailed() {
        AnalyticsEvent event = baseEvent("login_failed");
        assertDoesNotThrow(() -> riskEvaluationService.evaluate(event));
    }

    @Test
    @DisplayName("登录事件带riskContext可正常评估")
    void testEvaluateLoginWithRiskContext() {
        AnalyticsEvent event = baseEvent("login");
        AnalyticsEvent.RiskContext riskContext = new AnalyticsEvent.RiskContext();
        riskContext.setSessionId("session-1");
        event.setRiskContext(riskContext);

        assertDoesNotThrow(() -> riskEvaluationService.evaluate(event));
    }

    @Test
    @DisplayName("大额支付事件可正常评估")
    void testEvaluateLargePayment() {
        AnalyticsEvent event = baseEvent("payment_success");
        Map<String, Object> properties = new HashMap<>();
        properties.put("amount", 20000D);
        event.setProperties(properties);

        assertDoesNotThrow(() -> riskEvaluationService.evaluate(event));
    }

    @Test
    @DisplayName("支付金额为空时可正常评估")
    void testEvaluatePaymentWithoutAmount() {
        AnalyticsEvent event = baseEvent("payment_success");
        event.setProperties(new HashMap<>());

        assertDoesNotThrow(() -> riskEvaluationService.evaluate(event));
    }

    private AnalyticsEvent baseEvent(String type) {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setEventType(type);
        event.setUserId("user-1");
        event.setDeviceId("device-1");
        event.setProperties(new HashMap<>());
        return event;
    }
}
