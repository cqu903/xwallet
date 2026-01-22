package com.zerofinance.xwallet.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;

public class MqttJsonConverter extends DefaultPahoMessageConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Object extractPayload(Message<?> message) {
        byte[] payload = (byte[]) message.getPayload();
        String payloadStr = new String(payload, StandardCharsets.UTF_8);
        try {
            return objectMapper.readTree(payloadStr);
        } catch (Exception e) {
            return payloadStr;
        }
    }
}
