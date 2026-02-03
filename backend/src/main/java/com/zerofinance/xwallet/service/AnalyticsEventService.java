package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.AnalyticsEvent;
import com.zerofinance.xwallet.model.entity.AnalyticsEventEntity;
import com.zerofinance.xwallet.repository.AnalyticsEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MQTT埋点事件服务
 */
@Slf4j
@Service
public class AnalyticsEventService {

    private final AnalyticsEventMapper analyticsEventMapper;

    public AnalyticsEventService(AnalyticsEventMapper analyticsEventMapper) {
        this.analyticsEventMapper = analyticsEventMapper;
    }

    /**
     * 异步保存事件到数据库
     */
    @Async
    @Transactional
    public void saveEventAsync(AnalyticsEvent eventDto, String topic) {
        try {
            AnalyticsEventEntity entity = convertToEntity(eventDto, topic);
            analyticsEventMapper.insert(entity);
            log.debug("Event saved to database: {}", entity.getEventId());
        } catch (Exception e) {
            log.error("Failed to save event to database: {}", eventDto.getEventId(), e);
        }
    }

    /**
     * 根据事件ID查询
     */
    public AnalyticsEventEntity findByEventId(String eventId) {
        return analyticsEventMapper.findByEventId(eventId);
    }

    /**
     * 分页查询事件列表
     */
    public List<AnalyticsEventEntity> findEventsByConditions(
            String userId,
            String deviceId,
            String eventType,
            String environment,
            Boolean isCritical,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int offset,
            int limit
    ) {
        return analyticsEventMapper.findByConditions(
                userId, deviceId, eventType, environment, isCritical,
                startTime, endTime, offset, limit
        );
    }

    /**
     * 统计符合条件的记录数
     */
    public Long countByConditions(
            String userId,
            String deviceId,
            String eventType,
            String environment,
            Boolean isCritical,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        return analyticsEventMapper.countByConditions(
                userId, deviceId, eventType, environment, isCritical,
                startTime, endTime
        );
    }

    /**
     * 删除过期事件（定期清理）
     */
    @Transactional
    public int deleteOldEvents(int daysToKeep) {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(daysToKeep);
        analyticsEventMapper.deleteOldEvents(beforeTime);
        log.info("Deleted events older than {} days", daysToKeep);
        return 1; // 简化返回，实际可返回删除数量
    }

    /**
     * 将DTO转换为Entity
     */
    private AnalyticsEventEntity convertToEntity(AnalyticsEvent dto, String topic) {
        boolean isCritical = topic.contains("/critical");

        return AnalyticsEventEntity.builder()
                .eventId(dto.getEventId())
                .deviceId(dto.getDeviceId())
                .userId(dto.getUserId())
                .eventType(dto.getEventType())
                .environment(dto.getEnvironment() != null ? dto.getEnvironment() : "prod")
                .topic(topic)
                .payload(toJsonString(dto))
                .appVersion(dto.getContext() != null ? dto.getContext().getAppVersion() : null)
                .os(dto.getContext() != null ? dto.getContext().getOs() : null)
                .osVersion(dto.getContext() != null ? dto.getContext().getOsVersion() : null)
                .deviceModel(dto.getContext() != null ? dto.getContext().getDeviceModel() : null)
                .networkType(dto.getContext() != null ? dto.getContext().getNetworkType() : null)
                .sessionId(dto.getRiskContext() != null ? dto.getRiskContext().getSessionId() : null)
                .isCritical(isCritical)
                .receivedAt(System.currentTimeMillis())
                .eventTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : System.currentTimeMillis())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 简单的JSON序列化
     */
    private String toJsonString(AnalyticsEvent dto) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(dto);
        } catch (Exception e) {
            log.error("Failed to serialize event to JSON", e);
            return "{}";
        }
    }
}
