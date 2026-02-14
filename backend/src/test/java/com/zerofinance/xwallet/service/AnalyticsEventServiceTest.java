package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.AnalyticsEvent;
import com.zerofinance.xwallet.model.entity.AnalyticsEventEntity;
import com.zerofinance.xwallet.repository.AnalyticsEventMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("埋点事件服务单元测试")
class AnalyticsEventServiceTest {

    @Mock
    private AnalyticsEventMapper analyticsEventMapper;

    @InjectMocks
    private AnalyticsEventService analyticsEventService;

    @Test
    @DisplayName("异步保存事件成功")
    void testSaveEventAsyncSuccess() {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setEventId("evt-1");
        event.setDeviceId("dev-1");
        event.setUserId("u-1");
        event.setEventType("login");

        analyticsEventService.saveEventAsync(event, "app/dev/user_1");

        ArgumentCaptor<AnalyticsEventEntity> captor = ArgumentCaptor.forClass(AnalyticsEventEntity.class);
        verify(analyticsEventMapper).insert(captor.capture());
        AnalyticsEventEntity entity = captor.getValue();
        assertEquals("evt-1", entity.getEventId());
        assertEquals("prod", entity.getEnvironment());
        assertEquals("app/dev/user_1", entity.getTopic());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    @DisplayName("异步保存关键事件成功")
    void testSaveEventAsyncCriticalTopic() {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setEventId("evt-2");
        event.setDeviceId("dev-2");
        event.setUserId("u-2");
        event.setEventType("payment_success");
        event.setEnvironment("dev");

        analyticsEventService.saveEventAsync(event, "app/dev/critical/alert");

        ArgumentCaptor<AnalyticsEventEntity> captor = ArgumentCaptor.forClass(AnalyticsEventEntity.class);
        verify(analyticsEventMapper).insert(captor.capture());
        AnalyticsEventEntity entity = captor.getValue();
        assertTrue(Boolean.TRUE.equals(entity.getIsCritical()));
        assertEquals("dev", entity.getEnvironment());
    }

    @Test
    @DisplayName("异步保存异常时不中断")
    void testSaveEventAsyncException() {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setEventId("evt-3");
        doThrow(new RuntimeException("db down")).when(analyticsEventMapper).insert(org.mockito.ArgumentMatchers.any());

        analyticsEventService.saveEventAsync(event, "app/dev/user_3");
    }

    @Test
    @DisplayName("按事件ID查询")
    void testFindByEventId() {
        AnalyticsEventEntity entity = AnalyticsEventEntity.builder().eventId("evt-10").build();
        when(analyticsEventMapper.findByEventId("evt-10")).thenReturn(entity);

        AnalyticsEventEntity result = analyticsEventService.findByEventId("evt-10");

        assertSame(entity, result);
    }

    @Test
    @DisplayName("条件查询与计数")
    void testFindAndCountByConditions() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 14, 0, 0);
        List<AnalyticsEventEntity> events = List.of(AnalyticsEventEntity.builder().eventId("evt-a").build());
        when(analyticsEventMapper.findByConditions("u", "d", "login", "prod", true, start, end, 0, 10))
                .thenReturn(events);
        when(analyticsEventMapper.countByConditions("u", "d", "login", "prod", true, start, end))
                .thenReturn(7L);

        List<AnalyticsEventEntity> result = analyticsEventService.findEventsByConditions(
                "u", "d", "login", "prod", true, start, end, 0, 10
        );
        Long total = analyticsEventService.countByConditions("u", "d", "login", "prod", true, start, end);

        assertSame(events, result);
        assertEquals(7L, total);
    }

    @Test
    @DisplayName("删除过期事件")
    void testDeleteOldEvents() {
        int result = analyticsEventService.deleteOldEvents(30);

        assertEquals(1, result);
        verify(analyticsEventMapper).deleteOldEvents(org.mockito.ArgumentMatchers.any(LocalDateTime.class));
    }
}
