package com.finance.loans.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 首页数据汇总服务
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LoanService loanService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final BalanceService balanceService;

    /**
     * 获取首页所有数据
     */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 当前余额
        BigDecimal currentBalance = balanceService.getCurrentBalance();
        data.put("currentBalance", currentBalance);

        // 总负债
        BigDecimal totalDebt = loanService.getTotalDebt();
        data.put("totalDebt", totalDebt);

        // 本月收入
        BigDecimal monthlyIncome = incomeService.getMonthlyIncomeTotal();
        data.put("monthlyIncome", monthlyIncome);

        // 本月支出
        BigDecimal monthlyExpense = expenseService.getMonthlyExpenseTotal();
        data.put("monthlyExpense", monthlyExpense);

        // 本月还款
        BigDecimal monthlyPayment = loanService.getMonthlyPaymentTotal();
        data.put("monthlyPayment", monthlyPayment);

        // 本月结余
        BigDecimal monthlySurplus = monthlyIncome
                .subtract(monthlyExpense)
                .subtract(monthlyPayment);
        data.put("monthlySurplus", monthlySurplus);

        // 贷款汇总
        data.put("loanSummary", loanService.getLoanSummary());

        // 余额历史（最近一条）
        data.put("latestBalance", balanceService.getLatestBalanceRecord());

        return data;
    }

    /**
     * 获取财务概览
     */
    public Map<String, Object> getFinancialOverview() {
        Map<String, Object> overview = new HashMap<>();

        BigDecimal monthlyIncome = incomeService.getMonthlyIncomeTotal();
        BigDecimal monthlyExpense = expenseService.getMonthlyExpenseTotal();
        BigDecimal monthlyPayment = loanService.getMonthlyPaymentTotal();
        BigDecimal totalDebt = loanService.getTotalDebt();

        // 计算每月固定开销
        BigDecimal monthlyFixed = monthlyExpense.add(monthlyPayment);
        overview.put("monthlyFixedCost", monthlyFixed);

        // 计算结余
        BigDecimal surplus = monthlyIncome.subtract(monthlyFixed);
        overview.put("surplus", surplus);

        // 计算预计还清所有贷款需要的月数
        if (surplus.compareTo(BigDecimal.ZERO) > 0) {
            int monthsToFreedom = totalDebt.divide(surplus, 0, BigDecimal.ROUND_UP).intValue();
            overview.put("monthsToFreedom", monthsToFreedom);
        } else {
            overview.put("monthsToFreedom", -1); // 无法还清
        }

        return overview;
    }
}

