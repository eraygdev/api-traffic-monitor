package com.eray.apitrafficmonitor.interceptor;

import com.eray.apitrafficmonitor.model.RequestLog;
import com.eray.apitrafficmonitor.repository.LogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        // 1. Log kaydı oluştur
        RequestLog log = RequestLog.builder()
                .ipAddress(ipAddress)
                .endpoint(endpoint)
                .build();
        
        logRepository.save(log); // Veritabanına kaydet
        
        System.out.println("Gelen istek: IP=" + ipAddress + ", Endpoint=" + endpoint);
        
        return true; // true dönersek isteğin devam etmesine izin veririz
    }
}