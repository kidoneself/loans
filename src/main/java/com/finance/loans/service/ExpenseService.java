package com.finance.loans.service;

import com.finance.loans.mapper.ExpenseMapper;
import com.finance.loans.model.Expense;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

/**
 * 固定支出业务逻辑服务
 */
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseMapper expenseMapper;

    /**
     * 获取所有支出
     */
    public List<Expense> getAllExpenses() {
        return expenseMapper.findAll();
    }

    /**
     * 获取启用的支出项
     */
    public List<Expense> getActiveExpenses() {
        return expenseMapper.findByIsActive(true);
    }

    /**
     * 根据ID获取支出
     */
    public Expense getExpenseById(Long id) {
        Expense expense = expenseMapper.findById(id);
        if (expense == null) {
            throw new RuntimeException("支出记录不存在：" + id);
        }
        return expense;
    }

    /**
     * 添加支出
     */
    @Transactional
    public Expense addExpense(Expense expense) {
        expenseMapper.insert(expense);
        return expense;
    }

    /**
     * 更新支出
     */
    @Transactional
    public Expense updateExpense(Long id, Expense expenseDetails) {
        Expense expense = getExpenseById(id);
        expense.setExpenseName(expenseDetails.getExpenseName());
        expense.setAmount(expenseDetails.getAmount());
        expense.setExpenseDay(expenseDetails.getExpenseDay());
        expense.setIsActive(expenseDetails.getIsActive());
        expenseMapper.update(expense);
        return expense;
    }

    /**
     * 删除支出
     */
    @Transactional
    public void deleteExpense(Long id) {
        expenseMapper.deleteById(id);
    }

    /**
     * 获取每月支出总额
     */
    public BigDecimal getMonthlyExpenseTotal() {
        List<Expense> activeExpenses = expenseMapper.findByIsActive(true);
        return activeExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
