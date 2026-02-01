package com.zerofinance.xwallet.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;

public class MqttJsonConverter extends DefaultPahoMessageConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object extractPayload(Message<?> message) {
        byte[] payload = (byte[]) message.getPayload();
        String payloadStr = new String(payload, StandardCharsets.UTF_8);
        // 返回字符串，让 Spring Integration 自动转换
        // 不要在这里解析 JSON，否则会丢失消息头
        return payloadStr;
    }
}
