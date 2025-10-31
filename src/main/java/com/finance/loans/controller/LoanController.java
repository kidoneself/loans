package com.finance.loans.controller;

import com.finance.loans.model.Loan;
import com.finance.loans.model.PaymentHistory;
import com.finance.loans.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 贷款 REST API 控制器
 */
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoanController {

    private final LoanService loanService;

    /**
     * 获取所有贷款
     */
    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    /**
     * 获取活跃贷款
     */
    @GetMapping("/active")
    public ResponseEntity<List<Loan>> getActiveLoans() {
        return ResponseEntity.ok(loanService.getActiveLoans());
    }

    /**
     * 按平台分组获取贷款
     */
    @GetMapping("/by-platform")
    public ResponseEntity<Map<String, List<Loan>>> getLoansByPlatform() {
        return ResponseEntity.ok(loanService.getLoansByPlatform());
    }

    /**
     * 获取单个贷款
     */
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    /**
     * 添加贷款
     */
    @PostMapping
    public ResponseEntity<Loan> addLoan(@RequestBody Loan loan) {
        return ResponseEntity.ok(loanService.addLoan(loan));
    }

    /**
     * 更新贷款
     */
    @PutMapping("/{id}")
    public ResponseEntity<Loan> updateLoan(@PathVariable Long id, @RequestBody Loan loan) {
        return ResponseEntity.ok(loanService.updateLoan(id, loan));
    }

    /**
     * 删除贷款
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 记录还款
     */
    @PostMapping("/{id}/payment")
    public ResponseEntity<PaymentHistory> recordPayment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> paymentData) {
        
        // 支持多种参数名称
        BigDecimal amount = paymentData.containsKey("paymentAmount") 
            ? new BigDecimal(paymentData.get("paymentAmount").toString())
            : new BigDecimal(paymentData.get("amount").toString());
            
        LocalDate paymentDate = LocalDate.parse(paymentData.get("paymentDate").toString());
        
        boolean autoDeduct = paymentData.containsKey("autoDeductBalance")
            ? Boolean.parseBoolean(paymentData.get("autoDeductBalance").toString())
            : (paymentData.containsKey("autoDeduct") 
                ? Boolean.parseBoolean(paymentData.get("autoDeduct").toString()) 
                : true);
        
        String note = paymentData.get("note") != null ? paymentData.get("note").toString() : null;
        
        PaymentHistory payment = loanService.recordPayment(id, amount, paymentDate, autoDeduct, note);
        return ResponseEntity.ok(payment);
    }

    /**
     * 获取贷款的还款历史
     */
    @GetMapping("/{id}/payment-history")
    public ResponseEntity<List<PaymentHistory>> getPaymentHistory(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getPaymentHistory(id));
    }

    /**
     * 获取贷款汇总信息
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getLoanSummary() {
        return ResponseEntity.ok(loanService.getLoanSummary());
    }

    /**
     * 获取平台汇总
     */
    @GetMapping("/platform-summary")
    public ResponseEntity<List<Map<String, Object>>> getPlatformSummary() {
        return ResponseEntity.ok(loanService.getPlatformSummary());
    }

    /**
     * 获取贷款详情（含计算信息）
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<Map<String, Object>> getLoanDetail(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanDetail(id));
    }
}

