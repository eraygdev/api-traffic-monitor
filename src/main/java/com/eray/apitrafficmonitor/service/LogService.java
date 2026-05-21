package com.eray.apitrafficmonitor.service;

import com.eray.apitrafficmonitor.model.RequestLog;
import com.eray.apitrafficmonitor.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    @Async
    public void saveLog(String ip, String endpoint) {
        RequestLog log = new RequestLog();
        log.setIpAddress(ip);
        log.setEndpoint(endpoint);
        logRepository.save(log);
    }
}