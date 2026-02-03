package com.zerofinance.xwallet.mqtt;

import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;

/**
 * MQTT JSON 消息转换器
 *
 * 功能说明：
 * - 将 MQTT 消息的字节数组负载转换为 UTF-8 字符串
 * - 保留原始消息头信息（MQTT topic、QoS 等）
 * - 不在这里解析 JSON，让 Spring Integration 的消息转换流程自动处理
 *
 * 设计说明：
 * - 继承 DefaultPahoMessageConverter 并重写 extractPayload 方法
 * - 返回字符串而非对象，保持与 Spring Integration 的兼容性
 */
public class MqttJsonConverter extends DefaultPahoMessageConverter {

    /**
     * 提取消息负载并转换为 UTF-8 字符串
     *
     * 注意：Spring Integration 6.x 中 DefaultPahoMessageConverter 的实现方式已改变
     * 这里我们提供一个自定义的提取方法，但由于父类 API 限制，实际转换逻辑可能需要调整
     *
     * @param message 原始 MQTT 消息（负载为 byte[]）
     * @return UTF-8 编码的字符串负载
     */
    protected Object extractPayload(Message<?> message) {
        byte[] payload = (byte[]) message.getPayload();
        String payloadStr = new String(payload, StandardCharsets.UTF_8);
        // 返回字符串，让 Spring Integration 自动转换
        // 不要在这里解析 JSON，否则会丢失消息头
        return payloadStr;
    }
}
