package com.eray.apitrafficmonitor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrafficController {

    @GetMapping("/")
    public String home() {
        return "Sistem çalışıyor, trafik izleniyor!";
    }
}