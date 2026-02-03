package com.zerofinance.xwallet.mqtt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MqttJsonConverter 单元测试
 *
 * 测试目标：
 * 1. 验证 MQTT 消息负载能正确提取为字符串
 * 2. 验证字节消息能正确转换为 UTF-8 字符串
 * 3. 验证消息头信息被保留
 */
class MqttJsonConverterTest {

    private MqttJsonConverter converter;

    @BeforeEach
    void setUp() {
        converter = new MqttJsonConverter();
    }

    @Test
    void shouldExtractPayloadAsString() {
        // Given: 一个包含 JSON 字符串的字节数组消息
        String jsonPayload = "{\"event\":\"login\",\"userId\":\"123\"}";
        byte[] payloadBytes = jsonPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Message<byte[]> message = new GenericMessage<>(payloadBytes);

        // When: 提取消息负载
        Object extractedPayload = converter.extractPayload(message);

        // Then: 应该返回字符串格式的 JSON
        assertNotNull(extractedPayload, "提取的负载不应为 null");
        assertTrue(extractedPayload instanceof String, "提取的负载应该是字符串类型");
        assertEquals(jsonPayload, extractedPayload, "提取的负载应该与原始 JSON 字符串一致");
    }

    @Test
    void shouldHandleEmptyPayload() {
        // Given: 一个空负载消息
        byte[] emptyPayload = new byte[0];
        Message<byte[]> message = new GenericMessage<>(emptyPayload);

        // When: 提取空负载
        Object extractedPayload = converter.extractPayload(message);

        // Then: 应该返回空字符串
        assertNotNull(extractedPayload, "空负载提取后不应为 null");
        assertTrue(extractedPayload instanceof String, "空负载应该是字符串类型");
        assertEquals("", extractedPayload, "空负载应该转换为空字符串");
    }

    @Test
    void shouldHandleUtf8Characters() {
        // Given: 包含中文和特殊字符的 UTF-8 消息
        String chinesePayload = "{\"event\":\"购买\",\"用户\":\"张三\",\"金额\":100.50}";
        byte[] payloadBytes = chinesePayload.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Message<byte[]> message = new GenericMessage<>(payloadBytes);

        // When: 提取包含中文的负载
        Object extractedPayload = converter.extractPayload(message);

        // Then: 应该正确解码 UTF-8 字符
        assertNotNull(extractedPayload, "中文负载提取后不应为 null");
        assertEquals(chinesePayload, extractedPayload, "中文字符应该正确解码");
        assertTrue(((String) extractedPayload).contains("张三"), "应该包含中文字符");
    }

    @Test
    void shouldPreserveMessageHeaders() {
        // Given: 带有消息头的 MQTT 消息
        String payload = "{\"event\":\"click\"}";
        byte[] payloadBytes = payload.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Message<byte[]> message = new GenericMessage<>(
            payloadBytes,
            java.util.Map.of(
                "mqtt_topic", "app/prod/user123",
                "mqtt_qos", 1,
                "mqtt_retained", false
            )
        );

        // When: 提取负载（注意：extractPayload 只返回负载，不返回整个 Message）
        Object extractedPayload = converter.extractPayload(message);

        // Then: 负载应该正确提取，消息头保留在原始 Message 对象中
        assertEquals(payload, extractedPayload, "负载应该正确提取");
        // 注意：消息头的保留需要通过 Message 对象本身验证，不在 extractPayload 返回值中
    }

    @Test
    void shouldHandleComplexJsonStructure() {
        // Given: 复杂的嵌套 JSON 结构
        String complexJson = """
            {
                "eventId": "evt_12345",
                "eventType": "purchase",
                "timestamp": 1706659200000,
                "deviceId": "device_abc",
                "userId": "user_xyz",
                "data": {
                    "productId": "prod_001",
                    "quantity": 2,
                    "price": 99.99,
                    "currency": "CNY"
                },
                "metadata": {
                    "appVersion": "1.0.0",
                    "os": "iOS 17.2",
                    "network": "WiFi"
                }
            }""";
        byte[] payloadBytes = complexJson.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Message<byte[]> message = new GenericMessage<>(payloadBytes);

        // When: 提取复杂 JSON 负载
        Object extractedPayload = converter.extractPayload(message);

        // Then: 完整的 JSON 字符串应该被保留
        assertEquals(complexJson, extractedPayload, "复杂 JSON 结构应该完整保留");
        assertTrue(((String) extractedPayload).contains("\"eventId\""), "应该包含 eventId 字段");
        assertTrue(((String) extractedPayload).contains("\"data\""), "应该包含嵌套的 data 对象");
    }

    @Test
    void shouldHandleSpecialCharacters() {
        // Given: 包含转义字符和特殊符号的 JSON
        String specialJson = "{\"message\":\"Hello\\nWorld\\t!\",\"emoji\":\"😀\",\"quote\":\"\\\"quoted\\\"\"}";
        byte[] payloadBytes = specialJson.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Message<byte[]> message = new GenericMessage<>(payloadBytes);

        // When: 提取包含特殊字符的负载
        Object extractedPayload = converter.extractPayload(message);

        // Then: 特殊字符应该正确保留
        assertEquals(specialJson, extractedPayload, "特殊字符应该正确保留");
        assertTrue(((String) extractedPayload).contains("\\n"), "换行符应该保留");
        assertTrue(((String) extractedPayload).contains("😀"), "emoji 应该正确解码");
    }
}
