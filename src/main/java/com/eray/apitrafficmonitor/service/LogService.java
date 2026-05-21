package com.eray.apitrafficmonitor.service;

import com.eray.apitrafficmonitor.model.RequestLog;
import com.eray.apitrafficmonitor.repository.LogRepository;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    public enum LogType {
    SECURITY,
    ERROR,
    INFO,
    SYSTEM_ALERT
    }

    @Autowired
    private LogRepository logRepository;

    @Async
    public void saveLog(String ip, String endpoint, LogType logType, String message) {
        RequestLog log = new RequestLog();

        log.setIpAddress(ip);
        log.setEndpoint(endpoint);
        log.setReason(logType.name());
        log.setMessage(message);
        log.setTimestamp(LocalDateTime.now());
        
        logRepository.save(log);
    }
}