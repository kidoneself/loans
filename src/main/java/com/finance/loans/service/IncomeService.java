package com.finance.loans.service;

import com.finance.loans.mapper.IncomeMapper;
import com.finance.loans.model.Income;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

/**
 * 收入业务逻辑服务
 */
@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeMapper incomeMapper;

    /**
     * 获取所有收入
     */
    public List<Income> getAllIncome() {
        return incomeMapper.findAll();
    }

    /**
     * 获取启用的收入项
     */
    public List<Income> getActiveIncome() {
        return incomeMapper.findByIsActive(true);
    }

    /**
     * 根据ID获取收入
     */
    public Income getIncomeById(Long id) {
        Income income = incomeMapper.findById(id);
        if (income == null) {
            throw new RuntimeException("收入记录不存在：" + id);
        }
        return income;
    }

    /**
     * 添加收入
     */
    @Transactional
    public Income addIncome(Income income) {
        incomeMapper.insert(income);
        return income;
    }

    /**
     * 更新收入
     */
    @Transactional
    public Income updateIncome(Long id, Income incomeDetails) {
        Income income = getIncomeById(id);
        income.setIncomeType(incomeDetails.getIncomeType());
        income.setAmount(incomeDetails.getAmount());
        income.setIncomeDay(incomeDetails.getIncomeDay());
        income.setDescription(incomeDetails.getDescription());
        income.setIsActive(incomeDetails.getIsActive());
        incomeMapper.update(income);
        return income;
    }

    /**
     * 删除收入
     */
    @Transactional
    public void deleteIncome(Long id) {
        incomeMapper.deleteById(id);
    }

    /**
     * 获取每月收入总额
     */
    public BigDecimal getMonthlyIncomeTotal() {
        List<Income> activeIncome = incomeMapper.findByIsActive(true);
        return activeIncome.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
