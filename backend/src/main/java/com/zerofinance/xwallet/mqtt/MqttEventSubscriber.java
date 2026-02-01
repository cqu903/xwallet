package com.zerofinance.xwallet.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerofinance.xwallet.model.dto.AnalyticsEvent;
import com.zerofinance.xwallet.service.AnalyticsEventService;
import com.zerofinance.xwallet.service.EventDeduplicationService;
import com.zerofinance.xwallet.service.RiskEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
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
    private final AnalyticsEventService analyticsEventService;

    public MqttEventSubscriber(
        EventDeduplicationService deduplicationService,
        RiskEvaluationService riskEvaluationService,
        AnalyticsEventService analyticsEventService
    ) {
        this.deduplicationService = deduplicationService;
        this.riskEvaluationService = riskEvaluationService;
        this.analyticsEventService = analyticsEventService;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            // 调试：打印所有消息头
            log.debug("All message headers: {}", message.getHeaders());

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
        // 从消息头获取topic，Spring Integration MQTT 使用 mqtt_receivedTopic
        String topic = null;
        Object topicObj = message.getHeaders().get("mqtt_receivedTopic");

        if (topicObj != null) {
            topic = topicObj.toString();
        }

        // 如果 topic 仍为空，根据环境从 event 中推断
        if (topic == null || topic.isEmpty()) {
            String env = event.getEnvironment() != null ? event.getEnvironment() : "dev";
            // 由于我们不知道具体是哪个 topic (有多个通配符)，暂时标记为普通事件
            topic = "app/" + env + "/unknown";
            log.debug("Topic not found in headers, inferred: {}", topic);
        } else {
            log.debug("Topic extracted from headers: {}", topic);
        }

        final String finalTopic = topic;

        // 1. 保存事件到数据库（异步）
        analyticsEventService.saveEventAsync(event, finalTopic);

        // 2. 根据topic判断事件类型
        if (finalTopic.contains("/critical")) {
            // 风控事件，实时处理
            log.info("Critical event, triggering risk evaluation: {}", event.getEventType());
            riskEvaluationService.evaluate(event);
        } else {
            // 普通行为事件，已保存到数据库
            log.info("Behavior event: {} from user: {}", event.getEventType(), event.getUserId());
        }
    }
}
