package com.eray.apitrafficmonitor.repository;

import com.eray.apitrafficmonitor.model.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<RequestLog, Long> {
    // JpaRepository gives us save(), findAll(), delete() etc. methods!
}