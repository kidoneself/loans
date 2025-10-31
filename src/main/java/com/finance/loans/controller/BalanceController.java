package com.finance.loans.controller;

import com.finance.loans.model.BalanceHistory;
import com.finance.loans.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 余额 REST API 控制器
 */
@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BalanceController {

    private final BalanceService balanceService;

    /**
     * 获取当前余额
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentBalance() {
        BigDecimal balance = balanceService.getCurrentBalance();
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    /**
     * 获取余额历史记录
     */
    @GetMapping("/history")
    public ResponseEntity<List<BalanceHistory>> getBalanceHistory() {
        return ResponseEntity.ok(balanceService.getBalanceHistory());
    }

    /**
     * 更新余额
     */
    @PostMapping("/update")
    public ResponseEntity<BalanceHistory> updateBalance(@RequestBody Map<String, Object> data) {
        BigDecimal newBalance = new BigDecimal(data.get("balance").toString());
        String description = data.get("description") != null ? data.get("description").toString() : "手动更新";
        
        BalanceHistory history = balanceService.updateBalance(newBalance, description);
        return ResponseEntity.ok(history);
    }

    /**
     * 按类型查询余额历史
     */
    @GetMapping("/history/type/{type}")
    public ResponseEntity<List<BalanceHistory>> getHistoryByType(@PathVariable String type) {
        return ResponseEntity.ok(balanceService.getBalanceHistoryByType(type));
    }
}

