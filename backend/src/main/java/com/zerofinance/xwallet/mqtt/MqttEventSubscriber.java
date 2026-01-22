package com.zerofinance.xwallet.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerofinance.xwallet.model.dto.AnalyticsEvent;
import com.zerofinance.xwallet.service.EventDeduplicationService;
import com.zerofinance.xwallet.service.RiskEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttEventSubscriber implements MessageHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventDeduplicationService deduplicationService;
    private final RiskEvaluationService riskEvaluationService;

    public MqttEventSubscriber(
        EventDeduplicationService deduplicationService,
        RiskEvaluationService riskEvaluationService
    ) {
        this.deduplicationService = deduplicationService;
        this.riskEvaluationService = riskEvaluationService;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            String payload = message.getPayload().toString();
            AnalyticsEvent event = objectMapper.readValue(payload, AnalyticsEvent.class);

            log.info("Received event: {} from device: {}",
                event.getEventType(), event.getDeviceId());

            // 1. 去重
            if (deduplicationService.isDuplicate(event.getEventId())) {
                log.warn("Duplicate event ignored: {}", event.getEventId());
                return;
            }

            // 2. 处理事件
            processEvent(event, message);

        } catch (Exception e) {
            log.error("Failed to handle MQTT message", e);
            throw new MessagingException(message, e);
        }
    }

    private void processEvent(AnalyticsEvent event, Message<?> message) {
        // 从消息头获取topic
        Object topicObj = message.getHeaders().get("mqtt_topic");
        String topic = topicObj != null ? topicObj.toString() : "";

        // 根据topic判断事件类型
        if (topic.contains("/critical")) {
            // 风控事件，实时处理
            log.info("Critical event, triggering risk evaluation: {}", event.getEventType());
            riskEvaluationService.evaluate(event);
        } else {
            // 普通行为事件，异步处理（TODO）
            log.info("Behavior event: {} from user: {}", event.getEventType(), event.getUserId());
        }
    }
}
