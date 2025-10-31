package com.finance.loans.service;

import com.finance.loans.model.Expense;
import com.finance.loans.model.Income;
import com.finance.loans.model.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * 现金流预测服务（核心功能）
 */
@Service
@RequiredArgsConstructor
public class ForecastService {

    private final LoanService loanService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final BalanceService balanceService;

    /**
     * 预测未来N个月的固定收支
     */
    public List<Map<String, Object>> forecastCashFlow(int months) {
        List<Map<String, Object>> forecast = new ArrayList<>();
        
        // 获取所有数据
        List<Loan> activeLoans = loanService.getActiveLoans();
        List<Income> activeIncome = incomeService.getActiveIncome();
        List<Expense> activeExpenses = expenseService.getActiveExpenses();
        
        // 从当前月开始预测
        YearMonth currentMonth = YearMonth.now();
        
        // 用于累计计算长期趋势
        BigDecimal cumulativeBalance = BigDecimal.ZERO;
        
        for (int i = 0; i < months; i++) {
            YearMonth month = currentMonth.plusMonths(i);
            Map<String, Object> monthData = new HashMap<>();
            
            monthData.put("month", month.toString());
            monthData.put("monthDisplay", month.getYear() + "年" + month.getMonthValue() + "月");
            monthData.put("startBalance", cumulativeBalance);
            
            // 计算本月收入
            BigDecimal monthlyIncome = activeIncome.stream()
                    .map(Income::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthData.put("income", monthlyIncome);
            
            // 计算本月支出
            BigDecimal monthlyExpense = activeExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthData.put("expense", monthlyExpense);
            
            // 计算本月还款（需要判断贷款是否已还清）
            BigDecimal monthlyPayment = BigDecimal.ZERO;
            List<Map<String, Object>> paymentDetails = new ArrayList<>();
            
            for (Loan loan : activeLoans) {
                // 计算该贷款预计还清月份
                LocalDate payoffDate = loan.calculatePayoffDate();
                if (payoffDate != null) {
                    YearMonth payoffMonth = YearMonth.from(payoffDate);
                    // 如果当前预测月份 <= 还清月份，则需要还款
                    if (month.compareTo(payoffMonth) <= 0) {
                        monthlyPayment = monthlyPayment.add(loan.getMonthlyPayment());
                        
                        // 添加还款明细
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("loanName", loan.getLoanName());
                        detail.put("platform", loan.getPlatform());
                        detail.put("amount", loan.getMonthlyPayment());
                        detail.put("paymentDay", loan.getPaymentDay());
                        paymentDetails.add(detail);
                    }
                }
            }
            monthData.put("payment", monthlyPayment);
            monthData.put("paymentDetails", paymentDetails);
            
            // 计算结余
            BigDecimal surplus = monthlyIncome
                    .subtract(monthlyExpense)
                    .subtract(monthlyPayment);
            monthData.put("surplus", surplus);
            
            // 累计计算月末余额（只用于判断长期趋势）
            cumulativeBalance = cumulativeBalance.add(surplus);
            monthData.put("endBalance", cumulativeBalance);
            
            // 判断是否赤字（累计余额为负）
            boolean isDeficit = cumulativeBalance.compareTo(BigDecimal.ZERO) < 0;
            monthData.put("isDeficit", isDeficit);
            
            forecast.add(monthData);
        }
        
        return forecast;
    }

    /**
     * 检测赤字预警
     */
    public Map<String, Object> detectDeficit(int months) {
        List<Map<String, Object>> forecast = forecastCashFlow(months);
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> deficitMonths = new ArrayList<>();
        for (Map<String, Object> monthData : forecast) {
            if ((Boolean) monthData.get("isDeficit")) {
                deficitMonths.add(monthData);
            }
        }
        
        result.put("hasDeficit", !deficitMonths.isEmpty());
        result.put("deficitMonths", deficitMonths);
        
        if (!deficitMonths.isEmpty()) {
            Map<String, Object> firstDeficit = deficitMonths.get(0);
            result.put("firstDeficitMonth", firstDeficit.get("monthDisplay"));
            result.put("warning", "预计在 " + firstDeficit.get("monthDisplay") + " 会出现赤字");
        }
        
        return result;
    }

    /**
     * 生成资金事件时间线（精确到日）
     */
    public List<Map<String, Object>> generateEventTimeline(int months) {
        List<Map<String, Object>> events = new ArrayList<>();
        
        List<Loan> activeLoans = loanService.getActiveLoans();
        List<Income> activeIncome = incomeService.getActiveIncome();
        List<Expense> activeExpenses = expenseService.getActiveExpenses();
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(months);
        
        // 生成每个月的事件
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusMonths(1)) {
            // 收入事件
            for (Income income : activeIncome) {
                Map<String, Object> event = new HashMap<>();
                event.put("date", date.withDayOfMonth(income.getIncomeDay()));
                event.put("type", "income");
                event.put("amount", income.getAmount());
                event.put("source", income.getIncomeType());
                events.add(event);
            }
            
            // 支出事件
            for (Expense expense : activeExpenses) {
                Map<String, Object> event = new HashMap<>();
                event.put("date", date.withDayOfMonth(expense.getExpenseDay()));
                event.put("type", "expense");
                event.put("amount", expense.getAmount());
                event.put("source", expense.getExpenseName());
                events.add(event);
            }
            
            // 还款事件
            for (Loan loan : activeLoans) {
                LocalDate payoffDate = loan.calculatePayoffDate();
                if (payoffDate != null && date.isBefore(payoffDate.plusDays(1))) {
                    Map<String, Object> event = new HashMap<>();
                    event.put("date", date.withDayOfMonth(loan.getPaymentDay()));
                    event.put("type", "payment");
                    event.put("amount", loan.getMonthlyPayment());
                    event.put("source", loan.getLoanName());
                    events.add(event);
                }
            }
        }
        
        // 按日期排序
        events.sort((e1, e2) -> ((LocalDate) e1.get("date")).compareTo((LocalDate) e2.get("date")));
        
        return events;
    }
}

