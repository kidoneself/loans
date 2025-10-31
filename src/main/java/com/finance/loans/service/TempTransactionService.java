package com.finance.loans.service;

import com.finance.loans.mapper.TempTransactionMapper;
import com.finance.loans.model.TempTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 临时收支服务
 */
@Service
@RequiredArgsConstructor
public class TempTransactionService {

    private final TempTransactionMapper transactionMapper;

    /**
     * 获取所有临时收支
     */
    public List<TempTransaction> getAll() {
        return transactionMapper.findAll();
    }

    /**
     * 获取日期范围内的临时收支
     */
    public List<TempTransaction> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactionMapper.findByDateRange(startDate, endDate);
    }

    /**
     * 获取某周期的临时收入总额
     */
    public BigDecimal getTempIncomeInRange(LocalDate startDate, LocalDate endDate) {
        List<TempTransaction> incomes = transactionMapper.findByTypeAndDateRange("income", startDate, endDate);
        return incomes.stream()
                .map(TempTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取某周期的临时支出总额
     */
    public BigDecimal getTempExpenseInRange(LocalDate startDate, LocalDate endDate) {
        List<TempTransaction> expenses = transactionMapper.findByTypeAndDateRange("expense", startDate, endDate);
        return expenses.stream()
                .map(TempTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 添加临时收支
     */
    @Transactional
    public TempTransaction add(TempTransaction transaction) {
        transactionMapper.insert(transaction);
        return transaction;
    }

    /**
     * 删除临时收支
     */
    @Transactional
    public void delete(Long id) {
        transactionMapper.deleteById(id);
    }
}

