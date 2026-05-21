package com.eray.apitrafficmonitor.interceptor;

import com.eray.apitrafficmonitor.service.LogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private LogService logService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ipAddress = request.getRemoteAddr();
        String endpoint = request.getRequestURI();
        
        logService.saveLog(ipAddress, endpoint);
        
        System.out.println("Gelen istek: IP=" + ipAddress + ", Endpoint=" + endpoint);
        
        String key = "ratelimit:" + ipAddress;
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