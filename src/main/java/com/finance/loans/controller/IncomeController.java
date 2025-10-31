package com.finance.loans.controller;

import com.finance.loans.model.Income;
import com.finance.loans.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 收入 REST API 控制器
 */
@RestController
@RequestMapping("/api/income")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IncomeController {

    private final IncomeService incomeService;

    @GetMapping
    public ResponseEntity<List<Income>> getAllIncome() {
        return ResponseEntity.ok(incomeService.getAllIncome());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Income>> getActiveIncome() {
        return ResponseEntity.ok(incomeService.getActiveIncome());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Income> getIncomeById(@PathVariable Long id) {
        return ResponseEntity.ok(incomeService.getIncomeById(id));
    }

    @PostMapping
    public ResponseEntity<Income> addIncome(@RequestBody Income income) {
        return ResponseEntity.ok(incomeService.addIncome(income));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Income> updateIncome(@PathVariable Long id, @RequestBody Income income) {
        return ResponseEntity.ok(incomeService.updateIncome(id, income));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/monthly-total")
    public ResponseEntity<BigDecimal> getMonthlyTotal() {
        return ResponseEntity.ok(incomeService.getMonthlyIncomeTotal());
    }
}

