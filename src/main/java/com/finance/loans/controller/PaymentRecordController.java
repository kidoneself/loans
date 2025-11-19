package com.finance.loans.controller;

import com.finance.loans.model.PaymentRecord;
import com.finance.loans.service.PaymentRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 还款记录控制器
 */
@RestController
@RequestMapping("/api/payment-records")
public class PaymentRecordController {
    
    @Autowired
    private PaymentRecordService recordService;
    
    /**
     * 获取贷款的还款记录
     */
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<PaymentRecord>> getLoanRecords(@PathVariable Long loanId) {
        return ResponseEntity.ok(recordService.getLoanRecords(loanId));
    }
    
    /**
     * 获取本月还款记录
     */
    @GetMapping("/current-month")
    public ResponseEntity<List<PaymentRecord>> getCurrentMonthRecords() {
        return ResponseEntity.ok(recordService.getCurrentMonthRecords());
    }
    
    /**
     * 获取指定月份的还款记录
     */
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<PaymentRecord>> getMonthRecords(
            @PathVariable int year, 
            @PathVariable int month) {
        return ResponseEntity.ok(recordService.getMonthRecords(year, month));
    }
}
