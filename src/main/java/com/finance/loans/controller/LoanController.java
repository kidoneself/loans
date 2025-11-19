package com.finance.loans.controller;

import com.finance.loans.model.Loan;
import com.finance.loans.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 贷款控制器
 */
@RestController
@RequestMapping("/api/loans")
public class LoanController {
    
    @Autowired
    private LoanService loanService;
    
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
     * 根据ID获取贷款
     */
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        Loan loan = loanService.getLoanById(id);
        if (loan == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(loan);
    }
    
    /**
     * 添加贷款
     */
    @PostMapping
    public ResponseEntity<Loan> addLoan(@RequestBody Loan loan) {
        try {
            Loan created = loanService.addLoan(loan);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 更新贷款
     */
    @PutMapping("/{id}")
    public ResponseEntity<Loan> updateLoan(@PathVariable Long id, @RequestBody Loan loan) {
        try {
            Loan updated = loanService.updateLoan(id, loan);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 删除贷款
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteLoan(@PathVariable Long id) {
        try {
            loanService.deleteLoan(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 提前还清
     */
    @PostMapping("/{id}/early-settlement")
    public ResponseEntity<Map<String, String>> markAsEarlySettlement(@PathVariable Long id) {
        try {
            loanService.markAsEarlySettlement(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "提前还清成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取贷款统计
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getLoanSummary() {
        return ResponseEntity.ok(loanService.getLoanSummary());
    }
}
