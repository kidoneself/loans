package com.finance.loans.controller;

import com.finance.loans.service.LoanAutoUpdateService;
import com.finance.loans.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统管理 REST API 控制器
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SystemController {

    private final LoanAutoUpdateService loanAutoUpdateService;
    private final LoanService loanService;

    /**
     * 手动触发贷款期数自动更新
     */
    @PostMapping("/update-paid-periods")
    public ResponseEntity<Map<String, Object>> manualUpdatePaidPeriods() {
        loanAutoUpdateService.manualUpdatePaidPeriods();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已手动触发贷款期数更新");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取系统信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", "1.0.0");
        info.put("name", "个人资金 & 网贷管理系统");
        info.put("features", new String[]{
            "贷款管理",
            "收入支出管理",
            "还款计划",
            "固定收支预测",
            "工资周期预测",
            "还款日历",
            "债务趋势",
            "自动更新已还期数"
        });
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * 生成历史还款记录
     * 根据贷款的已还期数、开始日期等信息，自动生成历史还款记录到 payment_history 表
     * 
     * @param loanId 可选，指定贷款ID则只生成该贷款的记录，不指定则生成所有活跃贷款的记录
     */
    @PostMapping("/generate-payment-history")
    public ResponseEntity<Map<String, Object>> generatePaymentHistory(
            @RequestParam(required = false) Long loanId) {
        Map<String, Object> result = loanService.generatePaymentHistory(loanId);
        return ResponseEntity.ok(result);
    }
}

