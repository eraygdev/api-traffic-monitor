package com.eray.apitrafficmonitor.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.eray.apitrafficmonitor.constant.AppConstants;

@Component
public class SessionCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(SessionCleanupTask.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Scheduled(fixedRate = 60000)
    public void cleanup() {
        long count = 0;

        Cursor<byte[]> cursor = redisTemplate.executeWithStickyConnection(connection -> connection.keyCommands().scan(
                ScanOptions.scanOptions()
                        .match(AppConstants.ACTIVE_SESSION_PREFIX + "*")
                        .count(1000)
                        .build()));

        try {
            while (cursor.hasNext()) {
                cursor.next();
                count++;
            }
        } finally {
            cursor.close();
        }

        redisTemplate.opsForValue().set(AppConstants.ACTIVE_USERS_KEY, String.valueOf(count));
        logger.info("Active users: {}", count);
    }
}