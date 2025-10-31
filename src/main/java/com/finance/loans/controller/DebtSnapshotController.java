package com.finance.loans.controller;

import com.finance.loans.model.DebtSnapshot;
import com.finance.loans.service.DebtSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 债务快照控制器
 */
@RestController
@RequestMapping("/api/debt-snapshot")
@RequiredArgsConstructor
public class DebtSnapshotController {
    
    private final DebtSnapshotService snapshotService;
    
    /**
     * 手动创建今天的快照
     */
    @PostMapping("/create")
    public ResponseEntity<DebtSnapshot> createSnapshot() {
        DebtSnapshot snapshot = snapshotService.createSnapshot(LocalDate.now());
        return ResponseEntity.ok(snapshot);
    }
    
    /**
     * 获取日趋势（按天）
     */
    @GetMapping("/trend/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyTrend(
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> trend = snapshotService.getDailyTrend(days);
        return ResponseEntity.ok(trend);
    }
    
    /**
     * 获取月趋势（按月）
     */
    @GetMapping("/trend/monthly")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrend(
            @RequestParam(defaultValue = "12") int months) {
        List<Map<String, Object>> trend = snapshotService.getMonthlyTrend(months);
        return ResponseEntity.ok(trend);
    }
    
    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = snapshotService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}

