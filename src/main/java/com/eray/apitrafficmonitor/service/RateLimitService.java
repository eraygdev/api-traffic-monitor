package com.eray.apitrafficmonitor.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.eray.apitrafficmonitor.constant.AppConstants;

@Service
public class RateLimitService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean isRequestAllowed(String ipAddress) {
        String key = AppConstants.REDIS_KEY_PREFIX + ipAddress;
        long count = redisTemplate.opsForValue().increment(key);

        if(count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(AppConstants.RATE_LIMIT_WINDOW_MINUTES));
        }

        return count <= AppConstants.RATE_LIMIT_THRESHOLD;
    }

    public boolean isBlocked(String ipAddress) {
        String key = AppConstants.REDIS_KEY_PREFIX + ipAddress;
        String countStr = redisTemplate.opsForValue().get(key);
        if(countStr == null) return false;

        return Long.parseLong(countStr) > AppConstants.BLOCK_THRESHOLD;
    }

}
