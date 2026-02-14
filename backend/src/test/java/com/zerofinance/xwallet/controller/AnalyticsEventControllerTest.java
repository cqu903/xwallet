package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.entity.AnalyticsEventEntity;
import com.zerofinance.xwallet.service.AnalyticsEventService;
import com.zerofinance.xwallet.util.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("埋点事件控制器单元测试")
class AnalyticsEventControllerTest {

    @Mock
    private AnalyticsEventService analyticsEventService;

    @InjectMocks
    private AnalyticsEventController analyticsEventController;

    @Test
    @DisplayName("分页查询事件成功")
    void testGetEventListSuccess() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 13, 23, 59);
        List<AnalyticsEventEntity> events = List.of(
                AnalyticsEventEntity.builder().eventId("evt-1").eventType("login").build()
        );
        when(analyticsEventService.findEventsByConditions(
                "u1", "d1", "login", "prod", true, start, end, 10, 5
        )).thenReturn(events);
        when(analyticsEventService.countByConditions(
                "u1", "d1", "login", "prod", true, start, end
        )).thenReturn(42L);

        ResponseResult<Map<String, Object>> result = analyticsEventController.getEventList(
                "u1", "d1", "login", "prod", true, start, end, 2, 5
        );

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertSame(events, result.getData().get("list"));
        assertEquals(42L, result.getData().get("total"));
        assertEquals(2, result.getData().get("page"));
        assertEquals(5, result.getData().get("size"));
    }

    @Test
    @DisplayName("分页查询事件异常")
    void testGetEventListException() {
        when(analyticsEventService.findEventsByConditions(
                null, null, null, null, null, null, null, 0, 20
        )).thenThrow(new RuntimeException("query failed"));

        ResponseResult<Map<String, Object>> result = analyticsEventController.getEventList(
                null, null, null, null, null, null, null, 0, 20
        );

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().startsWith("查询事件列表失败: "));
        assertNull(result.getData());
    }

    @Test
    @DisplayName("根据ID查询事件成功")
    void testGetEventByIdSuccess() {
        AnalyticsEventEntity event = AnalyticsEventEntity.builder().eventId("evt-100").build();
        when(analyticsEventService.findByEventId("evt-100")).thenReturn(event);

        ResponseResult<AnalyticsEventEntity> result = analyticsEventController.getEventById("evt-100");

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertSame(event, result.getData());
        verify(analyticsEventService).findByEventId("evt-100");
    }

    @Test
    @DisplayName("根据ID查询事件不存在")
    void testGetEventByIdNotFound() {
        when(analyticsEventService.findByEventId("evt-not-found")).thenReturn(null);

        ResponseResult<AnalyticsEventEntity> result = analyticsEventController.getEventById("evt-not-found");

        assertEquals(404, result.getCode());
        assertEquals("事件不存在", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("根据ID查询事件异常")
    void testGetEventByIdException() {
        when(analyticsEventService.findByEventId("evt-ex")).thenThrow(new RuntimeException("db down"));

        ResponseResult<AnalyticsEventEntity> result = analyticsEventController.getEventById("evt-ex");

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().startsWith("获取事件详情失败: "));
        assertNull(result.getData());
    }
}
