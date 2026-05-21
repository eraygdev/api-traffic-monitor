package com.eray.apitrafficmonitor.interceptor;

import com.eray.apitrafficmonitor.service.LogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private static final String REDIS_KEY_PREFIX = "rl:";
    private static final String LOG_TEMPLATE_PREFIX = "Req: IP={}, Endp={}";
    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private LogService logService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ipAddress = request.getRemoteAddr();
        String endpoint = request.getRequestURI();

        logger.info(LOG_TEMPLATE_PREFIX, ipAddress, endpoint);    
        
        logService.saveLog(ipAddress, endpoint);
        
        String key = REDIS_KEY_PREFIX + ipAddress;
        Long count = redisTemplate.opsForValue().increment(key);

        if(count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        if(count > 5){
            response.setStatus(429);
            return false;
        }

        return true;
    }
}