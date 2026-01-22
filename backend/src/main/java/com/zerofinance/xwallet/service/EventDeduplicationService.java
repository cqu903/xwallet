package com.zerofinance.xwallet.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EventDeduplicationService {

    private final RedisTemplate<String, String> redisTemplate;

    public EventDeduplicationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isDuplicate(String eventId) {
        String key = "event:" + eventId;

        Boolean isNew = redisTemplate.opsForValue()
            .setIfAbsent(key, "1", Duration.ofHours(1));

        return Boolean.FALSE.equals(isNew);
    }
}
