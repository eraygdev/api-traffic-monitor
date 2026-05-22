package com.eray.apitrafficmonitor.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.eray.apitrafficmonitor.constant.AppConstants;

@Component
public class SessionCleanupTask {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Scheduled(fixedRate = 60000)
    public void cleanup() {

        long count = 0;
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(
            ScanOptions.scanOptions().match(AppConstants.ACTIVE_SESSION_PREFIX + "*").count(1000).build()
        );

        while(cursor.hasNext()) {
            cursor.next();
            count++;
        }

        redisTemplate.opsForValue().set(AppConstants.ACTIVE_USERS_KEY, String.valueOf(count));

    }
}