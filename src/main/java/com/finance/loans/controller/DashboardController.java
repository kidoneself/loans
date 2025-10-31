package com.finance.loans.controller;

import com.finance.loans.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 首页 REST API 控制器
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取首页所有数据
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        return ResponseEntity.ok(dashboardService.getDashboardData());
    }

    /**
     * 获取财务概览
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getFinancialOverview() {
        return ResponseEntity.ok(dashboardService.getFinancialOverview());
    }
}

