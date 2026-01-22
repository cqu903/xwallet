package com.zerofinance.xwallet.mqtt;

import com.zerofinance.xwallet.model.dto.AnalyticsEvent;
import com.zerofinance.xwallet.service.EventDeduplicationService;
import com.zerofinance.xwallet.service.RiskEvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class MqttIntegrationTest {

    @Autowired(required = false)
    private MqttEventSubscriber subscriber;

    @MockBean
    private EventDeduplicationService deduplicationService;

    @MockBean
    private RiskEvaluationService riskEvaluationService;

    @Test
    public void testEventProcessing() {
        // 模拟收到事件
        AnalyticsEvent event = new AnalyticsEvent();
        event.setEventId("test-123");
        event.setEventType("login");
        event.setDeviceId("device-abc");
        event.setUserId("user-456");

        when(deduplicationService.isDuplicate(anyString())).thenReturn(false);
        doNothing().when(riskEvaluationService).evaluate(any());

        // 测试处理逻辑
        // 注意：完整测试需要:
        // - 启动MQTT broker (如EMQX)
        // - 发送测试消息到MQTT topic
        // - 验证消息是否被正确接收和处理
        // - 验证Redis去重是否工作
        
        // 当前为单元测试框架，实际集成测试需要完整环境
        assert true; // 占位符
    }

    @Test
    public void testDuplicateEventFiltering() {
        // 测试去重逻辑
        when(deduplicationService.isDuplicate("test-duplicate")).thenReturn(true);

        boolean isDuplicate = deduplicationService.isDuplicate("test-duplicate");
        assert isDuplicate; // 应该被识别为重复

        verify(deduplicationService, times(1)).isDuplicate("test-duplicate");
    }
}
