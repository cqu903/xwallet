package com.zerofinance.xwallet.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("事件去重服务单元测试")
class EventDeduplicationServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("首次事件不是重复")
    void testIsDuplicateFalseWhenFirstSeen() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent("event:evt-1", "1", Duration.ofHours(1))).thenReturn(true);

        EventDeduplicationService service = new EventDeduplicationService(redisTemplate);
        boolean duplicate = service.isDuplicate("evt-1");

        assertFalse(duplicate);
        verify(valueOperations).setIfAbsent("event:evt-1", "1", Duration.ofHours(1));
    }

    @Test
    @DisplayName("重复事件返回true")
    void testIsDuplicateTrueWhenAlreadySeen() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent("event:evt-2", "1", Duration.ofHours(1))).thenReturn(false);

        EventDeduplicationService service = new EventDeduplicationService(redisTemplate);
        boolean duplicate = service.isDuplicate("evt-2");

        assertTrue(duplicate);
    }

    @Test
    @DisplayName("Redis返回null按非重复处理")
    void testIsDuplicateFalseWhenRedisReturnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent("event:evt-3", "1", Duration.ofHours(1))).thenReturn(null);

        EventDeduplicationService service = new EventDeduplicationService(redisTemplate);
        boolean duplicate = service.isDuplicate("evt-3");

        assertFalse(duplicate);
    }
}
