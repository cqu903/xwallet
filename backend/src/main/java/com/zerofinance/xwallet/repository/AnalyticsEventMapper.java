package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.AnalyticsEventEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MQTT埋点事件Mapper接口
 */
@Mapper
public interface AnalyticsEventMapper {

    /**
     * 插入新事件
     * @param event 事件信息
     */
    void insert(AnalyticsEventEntity event);

    /**
     * 根据事件ID查询
     * @param eventId 事件ID
     * @return 事件信息
     */
    AnalyticsEventEntity findByEventId(@Param("eventId") String eventId);

    /**
     * 分页查询事件列表
     * @param userId 用户ID（可选）
     * @param deviceId 设备ID（可选）
     * @param eventType 事件类型（可选）
     * @param environment 环境（可选）
     * @param isCritical 是否为关键事件（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 事件列表
     */
    List<AnalyticsEventEntity> findByConditions(
        @Param("userId") String userId,
        @Param("deviceId") String deviceId,
        @Param("eventType") String eventType,
        @Param("environment") String environment,
        @Param("isCritical") Boolean isCritical,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );

    /**
     * 统计符合条件的记录数
     */
    Long countByConditions(
        @Param("userId") String userId,
        @Param("deviceId") String deviceId,
        @Param("eventType") String eventType,
        @Param("environment") String environment,
        @Param("isCritical") Boolean isCritical,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 删除过期事件（清理数据）
     * @param beforeTime 此时间之前的事件
     */
    void deleteOldEvents(@Param("beforeTime") LocalDateTime beforeTime);
}
