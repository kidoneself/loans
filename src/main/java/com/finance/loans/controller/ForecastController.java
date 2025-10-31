package com.finance.loans.controller;

import com.finance.loans.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 现金流预测 REST API 控制器
 */
@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ForecastController {

    private final ForecastService forecastService;

    /**
     * 预测未来现金流
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> forecastCashFlow(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(forecastService.forecastCashFlow(months));
    }

    /**
     * 检测赤字预警
     */
    @GetMapping("/deficit")
    public ResponseEntity<Map<String, Object>> detectDeficit(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(forecastService.detectDeficit(months));
    }

    /**
     * 生成资金事件时间线
     */
    @GetMapping("/timeline")
    public ResponseEntity<List<Map<String, Object>>> getEventTimeline(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(forecastService.generateEventTimeline(months));
    }
}

