package com.zerofinance.xwallet.mqtt;

import com.zerofinance.xwallet.model.dto.AnalyticsEvent;
import com.zerofinance.xwallet.service.AnalyticsEventService;
import com.zerofinance.xwallet.service.EventDeduplicationService;
import com.zerofinance.xwallet.service.RiskEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * MQTT 集成测试
 *
 * 测试目标：
 * 1. 验证 MQTT 消息能够被正确接收和解析
 * 2. 验证事件去重逻辑工作正常
 * 3. 验证风险评估服务被正确调用
 * 4. 验证事件保存服务被正确调用
 *
 * 注意：完整的集成测试需要运行中的 MQTT broker
 * 此测试使用 Mock 模拟依赖服务的行为
 */
@SpringBootTest
class MqttIntegrationTest {

    @Autowired(required = false)
    private MqttEventSubscriber subscriber;

    @MockBean
    private EventDeduplicationService deduplicationService;

    @MockBean
    private RiskEvaluationService riskEvaluationService;

    @MockBean
    private AnalyticsEventService analyticsEventService;

    private AnalyticsEvent testEvent;
    private Message<String> testMessage;

    @BeforeEach
    void setUp() {
        // 创建测试事件
        testEvent = new AnalyticsEvent();
        testEvent.setEventId("evt_test_001");
        testEvent.setEventType("login");
        testEvent.setDeviceId("device_test_001");
        testEvent.setUserId("user_test_001");
        testEvent.setEnvironment("dev");
        testEvent.setTimestamp(System.currentTimeMillis());

        // 创建测试消息
        String jsonPayload = """
            {
                "eventId": "evt_test_001",
                "eventType": "login",
                "deviceId": "device_test_001",
                "userId": "user_test_001",
                "environment": "dev",
                "timestamp": %d
            }
            """.formatted(System.currentTimeMillis());

        testMessage = new GenericMessage<>(
            jsonPayload,
            java.util.Map.of(
                "mqtt_receivedTopic", "app/dev/user_test_001",
                "mqtt_qos", 1,
                "mqtt_retained", false
            )
        );
    }

    @Test
    void shouldProcessNewEventSuccessfully() {
        // Given: 新事件（非重复）
        when(deduplicationService.isDuplicate("evt_test_001")).thenReturn(false);
        doNothing().when(analyticsEventService).saveEventAsync(any(), anyString());
        doNothing().when(riskEvaluationService).evaluate(any());

        // When: 处理新事件
        try {
            subscriber.handleMessage(testMessage);
        } catch (Exception e) {
            // 忽略 MessagingException，我们只验证服务调用
        }

        // Then: 应该调用去重检查
        verify(deduplicationService, times(1)).isDuplicate("evt_test_001");

        // 应该保存事件
        verify(analyticsEventService, times(1)).saveEventAsync(any(), anyString());

        // 验证成功
        verify(deduplicationService).isDuplicate("evt_test_001");
    }

    @Test
    void shouldFilterDuplicateEvents() {
        // Given: 重复事件
        when(deduplicationService.isDuplicate("evt_test_001")).thenReturn(true);

        // When: 处理重复事件
        try {
            subscriber.handleMessage(testMessage);
        } catch (Exception e) {
            // 忽略异常
        }

        // Then: 应该检查去重
        verify(deduplicationService, times(1)).isDuplicate("evt_test_001");

        // 不应该保存重复事件
        verify(analyticsEventService, never()).saveEventAsync(any(), anyString());

        // 不应该触发风险评估
        verify(riskEvaluationService, never()).evaluate(any());
    }

    @Test
    void shouldEvaluateRiskForCriticalEvents() {
        // Given: 关键事件（包含 /critical 的 topic）
        when(deduplicationService.isDuplicate(anyString())).thenReturn(false);
        doNothing().when(analyticsEventService).saveEventAsync(any(), anyString());
        doNothing().when(riskEvaluationService).evaluate(any());

        Message<String> criticalMessage = new GenericMessage<>(
            testMessage.getPayload(),
            java.util.Map.of(
                "mqtt_receivedTopic", "app/dev/critical/alert",
                "mqtt_qos", 1
            )
        );

        // When: 处理关键事件
        try {
            subscriber.handleMessage(criticalMessage);
        } catch (Exception e) {
            // 忽略异常
        }

        // Then: 应该触发风险评估
        verify(riskEvaluationService, times(1)).evaluate(any());
    }

    @Test
    void shouldNotEvaluateRiskForNormalEvents() {
        // Given: 普通事件（不包含 /critical）
        when(deduplicationService.isDuplicate(anyString())).thenReturn(false);
        doNothing().when(analyticsEventService).saveEventAsync(any(), anyString());

        // When: 处理普通事件
        try {
            subscriber.handleMessage(testMessage);
        } catch (Exception e) {
            // 忽略异常
        }

        // Then: 不应该触发风险评估
        verify(riskEvaluationService, never()).evaluate(any());
    }

    @Test
    void shouldHandleEventsWithDifferentTopics() {
        // Given: 来自不同环境的事件
        when(deduplicationService.isDuplicate(anyString())).thenReturn(false);
        doNothing().when(analyticsEventService).saveEventAsync(any(), anyString());

        // 测试生产环境事件
        Message<String> prodMessage = new GenericMessage<>(
            testMessage.getPayload(),
            java.util.Map.of("mqtt_receivedTopic", "app/prod/user_123")
        );

        try {
            subscriber.handleMessage(prodMessage);
        } catch (Exception e) {
            // 忽略异常
        }

        // 应该保存到正确的 topic
        verify(analyticsEventService, times(1)).saveEventAsync(any(), eq("app/prod/user_123"));
    }
}
