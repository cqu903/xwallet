package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.annotation.RequirePermission;
import com.zerofinance.xwallet.model.entity.AnalyticsEventEntity;
import com.zerofinance.xwallet.service.AnalyticsEventService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * MQTT埋点事件控制器
 * 用于查询和管理移动端上报的埋点事件
 */
@Tag(name = "MQTT事件管理", description = "查询移动端上报的埋点事件；需权限：system:mqtt:query")
@Slf4j
@RestController
@RequestMapping("/analytics/events")
@RequiredArgsConstructor
public class AnalyticsEventController {

    private final AnalyticsEventService analyticsEventService;

    @Operation(summary = "分页查询事件列表", description = "按用户ID、设备ID、事件类型、环境、时间范围等条件分页查询")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "401", description = "未登录"),
        @ApiResponse(responseCode = "403", description = "无 system:mqtt:query 权限"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/list")
    @RequirePermission("system:mqtt:query")
    public ResponseResult<Map<String, Object>> getEventList(
            @Parameter(description = "用户ID") @RequestParam(required = false) String userId,
            @Parameter(description = "设备ID") @RequestParam(required = false) String deviceId,
            @Parameter(description = "事件类型") @RequestParam(required = false) String eventType,
            @Parameter(description = "环境（prod/dev）") @RequestParam(required = false) String environment,
            @Parameter(description = "是否为关键事件") @RequestParam(required = false) Boolean isCritical,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size
    ) {
        log.info("收到查询事件列表请求 - userId: {}, deviceId: {}, eventType: {}, page: {}, size: {}",
                userId, deviceId, eventType, page, size);

        try {
            int offset = page * size;
            List<AnalyticsEventEntity> events = analyticsEventService.findEventsByConditions(
                    userId, deviceId, eventType, environment, isCritical,
                    startTime, endTime, offset, size
            );
            Long total = analyticsEventService.countByConditions(
                    userId, deviceId, eventType, environment, isCritical,
                    startTime, endTime
            );

            return ResponseResult.success(Map.of(
                    "list", events,
                    "total", total,
                    "page", page,
                    "size", size
            ));
        } catch (Exception e) {
            log.error("查询事件列表失败", e);
            return ResponseResult.error(500, "查询事件列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据事件ID查询详情", description = "返回单个事件的完整信息，包括payload")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "401", description = "未登录"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "事件不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/{eventId}")
    @RequirePermission("system:mqtt:query")
    public ResponseResult<AnalyticsEventEntity> getEventById(
            @Parameter(description = "事件ID") @PathVariable String eventId
    ) {
        log.info("收到获取事件详情请求 - eventId: {}", eventId);

        try {
            AnalyticsEventEntity event = analyticsEventService.findByEventId(eventId);
            if (event == null) {
                return ResponseResult.error(404, "事件不存在");
            }
            return ResponseResult.success(event);
        } catch (Exception e) {
            log.error("获取事件详情失败", e);
            return ResponseResult.error(500, "获取事件详情失败: " + e.getMessage());
        }
    }
}
