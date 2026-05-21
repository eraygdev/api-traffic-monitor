package com.eray.apitrafficmonitor.interceptor;

import com.eray.apitrafficmonitor.service.LogService;
import com.eray.apitrafficmonitor.service.LogService.LogType;

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
    /*  --- Log Templates ---
                         Template:     Req: [IP] | [Endpoint] | [Result] | [Active Users] */
    private static final String LOG_TEMPLATE_S = "Success: {} | {} | {} | {}";
    private static final String LOG_TEMPLATE_E = "Error  : {} | {} | {} | {}";
    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    // --- Redis Configuration Keys ---
    private static final String ACTIVE_USERS_KEY = "global:active_users";
    private static final String REDIS_KEY_PREFIX = "rl:";      // Rate limit per IP
    private static final String ACTIVE_SESSION_PREFIX = "as:"; // Session tracking
    private static final String NEW_USER_ATTRIBUTE = "is_new"; 
    private static final String SESSION_COOKIE_NAME = "user_token";
    
    // --- Limit Thresholds ---
    private static final int MAX_ACTIVE_USER = 30000;
    private static final int RATE_LIMIT_THRESHOLD = 5;
    private static final int BLOCK_THRESHOLD = 50;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private LogService logService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String sessionId = getSessionIdFromCookie(request);

        if (sessionId != null && redisTemplate.hasKey(ACTIVE_SESSION_PREFIX + sessionId)) {
            return true;
        }

        request.setAttribute(NEW_USER_ATTRIBUTE, true);
        if(redisTemplate.opsForValue().increment(ACTIVE_USERS_KEY) >= MAX_ACTIVE_USER) {
            redisTemplate.opsForValue().decrement(ACTIVE_USERS_KEY);

            response.setStatus(503);
            return false;
        }

        redisTemplate.opsForValue().set(ACTIVE_SESSION_PREFIX + sessionId, "1", Duration.ofMinutes(5));

        String ipAddress = request.getRemoteAddr();
        String endpoint = request.getRequestURI();
        
        String key = REDIS_KEY_PREFIX + ipAddress;
        long count = redisTemplate.opsForValue().increment(key);

        if(count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        if(count > BLOCK_THRESHOLD){
            response.setStatus(403);
            logService.saveLog(ipAddress, endpoint, LogType.SECURITY, "Request limit exceeded! :" + count);
            return false;
        }   

        else if(count > RATE_LIMIT_THRESHOLD){
            response.setStatus(429);
            return false;
        }   
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        if (request.getAttribute(NEW_USER_ATTRIBUTE) != null) {
            long count = redisTemplate.opsForValue().decrement(ACTIVE_USERS_KEY);
            String ip = request.getRemoteAddr();
            String uri = request.getRequestURI();
            int status = response.getStatus();

            if (e != null) {
                logger.error(LOG_TEMPLATE_E, ip, uri, status, count + 1);
            } else {
                logger.info(LOG_TEMPLATE_S, ip, uri, status, count + 1);
            }
        }
    }

    private String getSessionIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}