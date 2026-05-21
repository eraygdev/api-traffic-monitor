package com.eray.apitrafficmonitor.interceptor;

import com.eray.apitrafficmonitor.model.RequestLog;
import com.eray.apitrafficmonitor.repository.LogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private LogRepository logRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ipAddress = request.getRemoteAddr();
        String endpoint = request.getRequestURI();

        RequestLog log = new RequestLog();
        log.setIpAddress(ipAddress);
        log.setEndpoint(endpoint);
        
        logRepository.save(log); 
        
        System.out.println("Gelen istek: IP=" + ipAddress + ", Endpoint=" + endpoint);
        
        LocalDateTime oneMinAgo =  LocalDateTime.now().minusMinutes(1);
        long rCount = logRepository.countByIpAddressAndTimestampAfter(ipAddress, oneMinAgo);

        if(rCount < 10) {
            
        }

        return true;
    }
}