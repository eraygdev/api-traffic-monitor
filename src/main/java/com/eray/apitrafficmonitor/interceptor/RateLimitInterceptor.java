package com.eray.apitrafficmonitor.interceptor;

import com.eray.apitrafficmonitor.constant.AppConstants;
import com.eray.apitrafficmonitor.service.LogService;
import com.eray.apitrafficmonitor.service.RateLimitService;
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
                                       Req: [IP] | [Endpoint] | [Result] | [Active Users] */
    private static final String LOG_TEMPLATE_S = "Success: {} | {} | {} | {}";
    private static final String LOG_TEMPLATE_E = "Error  : {} | {} | {} | {}";
    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RateLimitService rateLimitService;

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
        
        String activeUsersStr = redisTemplate.opsForValue().get(AppConstants.ACTIVE_USERS_KEY);
        if (activeUsersStr != null && Long.parseLong(activeUsersStr) >= AppConstants.MAX_ACTIVE_USER) {
            response.setStatus(503);
            return false;
        }

        String ip = request.getRemoteAddr();

        if(rateLimitService.isBlocked(ip)){
            response.setStatus(403);
            logService.saveLog(ip, request.getRequestURI(), LogType.SECURITY, "Blocked IP access attempt.");
            return false;
        }   

        else if(!rateLimitService.isRequestAllowed(ip)){
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