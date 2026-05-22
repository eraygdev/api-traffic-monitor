package com.eray.apitrafficmonitor.interceptor;

import com.eray.apitrafficmonitor.constant.AppConstants;
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

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private LogService logService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String sessionId = getSessionIdFromCookie(request);

        if (sessionId != null) {
            String redisKey = AppConstants.ACTIVE_SESSION_PREFIX + sessionId;
        
            if (redisTemplate.hasKey(redisKey)) {
                redisTemplate.expire(redisKey, Duration.ofMinutes(AppConstants.SESSION_TIMEOUT_MINUTES));
                return true;
            }
        }

        request.setAttribute(AppConstants.NEW_USER_ATTRIBUTE, true);
        if(redisTemplate.opsForValue().increment(AppConstants.ACTIVE_USERS_KEY) >= AppConstants.MAX_ACTIVE_USER) {
            redisTemplate.opsForValue().decrement(AppConstants.ACTIVE_USERS_KEY);

            response.setStatus(503);
            return false;
        }

        String ipAddress = request.getRemoteAddr();
        String endpoint = request.getRequestURI();
        
        String key = AppConstants.REDIS_KEY_PREFIX + ipAddress;
        long count = redisTemplate.opsForValue().increment(key);

        if(count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(AppConstants.RATE_LIMIT_WINDOW_MINUTES));
        }

        if(count > AppConstants.BLOCK_THRESHOLD){
            response.setStatus(403);
            logService.saveLog(ipAddress, endpoint, LogType.SECURITY, "Request limit exceeded! :" + count);
            return false;
        }   

        else if(count > AppConstants.RATE_LIMIT_THRESHOLD){
            response.setStatus(429);
            return false;
        }   
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        if (request.getAttribute(AppConstants.NEW_USER_ATTRIBUTE) != null) {
            String ip = request.getRemoteAddr();
            String uri = request.getRequestURI();
            int status = response.getStatus();

            if (e != null) {
                logger.error(LOG_TEMPLATE_E, ip, uri, status);
            } else {
                logger.info(LOG_TEMPLATE_S, ip, uri, status);
            }
        }
    }

    private String getSessionIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (AppConstants.SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}