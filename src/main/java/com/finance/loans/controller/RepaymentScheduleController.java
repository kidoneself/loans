package com.finance.loans.controller;

import com.finance.loans.model.RepaymentSchedule;
import com.finance.loans.service.RepaymentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 还款计划控制器
 */
@RestController
@RequestMapping("/api/schedules")
public class RepaymentScheduleController {
    
    @Autowired
    private RepaymentScheduleService scheduleService;
    
    /**
     * 获取贷款的还款计划
     */
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<RepaymentSchedule>> getLoanSchedules(@PathVariable Long loanId) {
        return ResponseEntity.ok(scheduleService.getLoanSchedules(loanId));
    }
    
    /**
     * 获取待还计划
     */
    @GetMapping("/loan/{loanId}/pending")
    public ResponseEntity<List<RepaymentSchedule>> getPendingSchedules(@PathVariable Long loanId) {
        return ResponseEntity.ok(scheduleService.getPendingSchedules(loanId));
    }
    
    /**
     * 获取本月还款计划
     */
    @GetMapping("/current-month")
    public ResponseEntity<List<RepaymentSchedule>> getCurrentMonthSchedules() {
        return ResponseEntity.ok(scheduleService.getCurrentMonthSchedules());
    }
    
    /**
     * 获取指定月份的还款计划
     */
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<RepaymentSchedule>> getMonthSchedules(
            @PathVariable int year, 
            @PathVariable int month) {
        List<RepaymentSchedule> schedules = scheduleService.getMonthSchedules(year, month);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * 记录还款
     */
    @PostMapping("/{scheduleId}/pay")
    public ResponseEntity<Map<String, String>> recordPayment(
            @PathVariable Long scheduleId,
            @RequestParam BigDecimal amount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate) {
        try {
            scheduleService.recordPayment(scheduleId, amount, paymentDate);
            Map<String, String> response = new HashMap<>();
            response.put("message", "还款记录成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 一键标记当天应还款项为已还
     */
    @PostMapping("/mark-today-paid")
    public ResponseEntity<Map<String, Object>> markTodayAsPaid() {
        try {
            int count = scheduleService.markTodayAsPaid();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "标记成功");
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
