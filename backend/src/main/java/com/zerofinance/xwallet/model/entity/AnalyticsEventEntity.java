package com.zerofinance.xwallet.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MQTT 埋点事件实体（数据库表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEventEntity {
    private Long id;
    private String eventId;
    private String deviceId;
    private String userId;
    private String eventType;
    private String environment;
    private String topic;
    private String payload;

    // 上下文信息
    private String appVersion;
    private String os;
    private String osVersion;
    private String deviceModel;
    private String networkType;

    // 风控相关
    private String sessionId;
    private Boolean isCritical;

    // 时间戳
    private Long receivedAt;
    private Long eventTimestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
