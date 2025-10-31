package com.finance.loans.controller;

import com.finance.loans.model.TempTransaction;
import com.finance.loans.service.SalaryCycleService;
import com.finance.loans.service.SystemConfigService;
import com.finance.loans.service.TempTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工资周期预测控制器
 */
@RestController
@RequestMapping("/api/salary-cycle")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SalaryCycleController {

    private final SalaryCycleService salaryCycleService;
    private final SystemConfigService configService;
    private final TempTransactionService tempTransactionService;

    /**
     * 获取当前工资周期预测
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentCycle() {
        return ResponseEntity.ok(salaryCycleService.getCurrentCycle());
    }

    /**
     * 获取未来N个周期的预测
     */
    @GetMapping("/future/{count}")
    public ResponseEntity<List<Map<String, Object>>> getFutureCycles(@PathVariable int count) {
        return ResponseEntity.ok(salaryCycleService.getFutureCycles(count));
    }

    /**
     * 获取工资日设置
     */
    @GetMapping("/config/salary-day")
    public ResponseEntity<Integer> getSalaryDay() {
        return ResponseEntity.ok(configService.getSalaryDay());
    }

    /**
     * 设置工资日
     */
    @PutMapping("/config/salary-day")
    public ResponseEntity<Void> setSalaryDay(@RequestBody Map<String, Integer> data) {
        configService.setSalaryDay(data.get("day"));
        return ResponseEntity.ok().build();
    }

    /**
     * 获取工资金额
     */
    @GetMapping("/config/salary-amount")
    public ResponseEntity<Double> getSalaryAmount() {
        return ResponseEntity.ok(configService.getSalaryAmount());
    }

    /**
     * 设置工资金额
     */
    @PutMapping("/config/salary-amount")
    public ResponseEntity<Void> setSalaryAmount(@RequestBody Map<String, Double> data) {
        configService.setSalaryAmount(data.get("amount"));
        return ResponseEntity.ok().build();
    }

    /**
     * 获取所有临时收支
     */
    @GetMapping("/temp-transactions")
    public ResponseEntity<List<TempTransaction>> getAllTempTransactions() {
        return ResponseEntity.ok(tempTransactionService.getAll());
    }

    /**
     * 添加临时收支
     */
    @PostMapping("/temp-transactions")
    public ResponseEntity<TempTransaction> addTempTransaction(@RequestBody TempTransaction transaction) {
        return ResponseEntity.ok(tempTransactionService.add(transaction));
    }

    /**
     * 删除临时收支
     */
    @DeleteMapping("/temp-transactions/{id}")
    public ResponseEntity<Void> deleteTempTransaction(@PathVariable Long id) {
        tempTransactionService.delete(id);
        return ResponseEntity.ok().build();
    }
}

