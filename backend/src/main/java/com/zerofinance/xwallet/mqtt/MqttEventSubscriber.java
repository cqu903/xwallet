package com.zerofinance.xwallet.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerofinance.xwallet.model.dto.AnalyticsEvent;
import com.zerofinance.xwallet.service.EventDeduplicationService;
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

    public MqttEventSubscriber(EventDeduplicationService deduplicationService) {
        this.deduplicationService = deduplicationService;
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

            // 2. 处理事件（TODO: Task 7）
            processEvent(event);

        } catch (Exception e) {
            log.error("Failed to handle MQTT message", e);
            throw new MessagingException(message, e);
        }
    }

    private void processEvent(AnalyticsEvent event) {
        // TODO: 调用风控服务、分析服务等
        log.info("Processing event: {}", event.getEventType());
    }
}
