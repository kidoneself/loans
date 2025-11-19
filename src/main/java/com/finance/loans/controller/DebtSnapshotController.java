package com.finance.loans.controller;

import com.finance.loans.model.DebtSnapshot;
import com.finance.loans.service.DebtSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负债快照控制器
 */
@RestController
@RequestMapping("/api/snapshots")
public class DebtSnapshotController {
    
    @Autowired
    private DebtSnapshotService snapshotService;
    
    /**
     * 获取所有快照
     */
    @GetMapping
    public ResponseEntity<List<DebtSnapshot>> getAllSnapshots() {
        return ResponseEntity.ok(snapshotService.getAllSnapshots());
    }
    
    /**
     * 获取最新快照
     */
    @GetMapping("/latest")
    public ResponseEntity<DebtSnapshot> getLatestSnapshot() {
        DebtSnapshot snapshot = snapshotService.getLatestSnapshot();
        if (snapshot == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(snapshot);
    }
    
    /**
     * 获取最近N天的快照
     */
    @GetMapping("/recent/{days}")
    public ResponseEntity<List<DebtSnapshot>> getRecentSnapshots(@PathVariable int days) {
        return ResponseEntity.ok(snapshotService.getRecentSnapshots(days));
    }
    
    /**
     * 获取指定日期范围的快照
     */
    @GetMapping("/range")
    public ResponseEntity<List<DebtSnapshot>> getSnapshotsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(snapshotService.getSnapshotsByDateRange(startDate, endDate));
    }
    
    /**
     * 手动创建快照
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createSnapshot() {
        try {
            snapshotService.createSnapshot(LocalDate.now(), "daily");
            Map<String, String> response = new HashMap<>();
            response.put("message", "快照创建成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
