package com.finance.loans.service;

import com.finance.loans.model.Expense;
import com.finance.loans.model.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 工资周期预测服务
 */
@Service
@RequiredArgsConstructor
public class SalaryCycleService {

    private final SystemConfigService configService;
    private final LoanService loanService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final TempTransactionService tempTransactionService;
    private final BalanceService balanceService;

    /**
     * 获取当前工资周期
     */
    public Map<String, Object> getCurrentCycle() {
        int salaryDay = configService.getSalaryDay();
        LocalDate today = LocalDate.now();
        
        // 计算周期开始和结束日期
        LocalDate cycleStart, cycleEnd;
        
        if (today.getDayOfMonth() >= salaryDay) {
            // 当前周期：本月salaryDay ~ 下月salaryDay
            cycleStart = LocalDate.of(today.getYear(), today.getMonth(), salaryDay);
            cycleEnd = cycleStart.plusMonths(1);
        } else {
            // 当前周期：上月salaryDay ~ 本月salaryDay
            cycleEnd = LocalDate.of(today.getYear(), today.getMonth(), salaryDay);
            cycleStart = cycleEnd.minusMonths(1);
        }
        
        return calculateCycle(cycleStart, cycleEnd, true);
    }

    /**
     * 获取指定周期的预测
     */
    public Map<String, Object> getCycleByDate(LocalDate date) {
        int salaryDay = configService.getSalaryDay();
        
        LocalDate cycleStart, cycleEnd;
        
        if (date.getDayOfMonth() >= salaryDay) {
            cycleStart = LocalDate.of(date.getYear(), date.getMonth(), salaryDay);
            cycleEnd = cycleStart.plusMonths(1);
        } else {
            cycleEnd = LocalDate.of(date.getYear(), date.getMonth(), salaryDay);
            cycleStart = cycleEnd.minusMonths(1);
        }
        
        return calculateCycle(cycleStart, cycleEnd, false);
    }

    /**
     * 计算周期数据
     */
    private Map<String, Object> calculateCycle(LocalDate cycleStart, LocalDate cycleEnd, boolean isCurrent) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        
        result.put("cycleStart", cycleStart.toString());
        result.put("cycleEnd", cycleEnd.toString());
        result.put("isCurrent", isCurrent);
        
        if (isCurrent) {
            result.put("daysToSalary", java.time.temporal.ChronoUnit.DAYS.between(today, cycleEnd));
        }
        
        // 获取当前余额
        BigDecimal currentBalance = balanceService.getCurrentBalance();
        result.put("currentBalance", currentBalance);
        
        // 工资收入
        double salaryAmount = configService.getSalaryAmount();
        result.put("salaryIncome", salaryAmount);
        
        // 计算这个周期内的贷款还款
        BigDecimal loanPayments = calculateLoanPayments(cycleStart, cycleEnd);
        result.put("loanPayments", loanPayments);
        
        // 计算这个周期内的固定支出
        BigDecimal fixedExpenses = calculateFixedExpenses(cycleStart, cycleEnd);
        result.put("fixedExpenses", fixedExpenses);
        
        // 对于当前周期，只计算从今天到周期结束的支出
        LocalDate calculateStart = isCurrent ? today : cycleStart;
        
        // 重新计算从今天开始的支出
        BigDecimal loanPaymentsFromToday = isCurrent ? 
            calculateLoanPaymentsInRange(today, cycleEnd) : loanPayments;
        BigDecimal fixedExpensesFromToday = isCurrent ? 
            calculateFixedExpensesInRange(today, cycleEnd) : fixedExpenses;
        
        // 获取这个周期的临时收入（从今天开始）
        BigDecimal tempIncome = tempTransactionService.getTempIncomeInRange(calculateStart, cycleEnd);
        result.put("tempIncome", tempIncome);
        
        // 获取这个周期的临时支出（从今天开始）
        BigDecimal tempExpense = tempTransactionService.getTempExpenseInRange(calculateStart, cycleEnd);
        result.put("tempExpense", tempExpense);
        
        // 更新结果中的支出金额（用从今天开始的）
        if (isCurrent) {
            result.put("loanPayments", loanPaymentsFromToday);
            result.put("fixedExpenses", fixedExpensesFromToday);
        }
        
        // 添加明细列表（对于当前周期，从今天开始）
        LocalDate detailStart = isCurrent ? today : cycleStart;
        result.put("loanPaymentDetails", getLoanPaymentDetails(detailStart, cycleEnd));
        result.put("fixedExpenseDetails", getFixedExpenseDetails(detailStart, cycleEnd));
        result.put("tempTransactions", tempTransactionService.getByDateRange(detailStart, cycleEnd));
        
        // 生成时间线（从今天或周期开始）
        List<Map<String, Object>> timeline = generateTimeline(detailStart, cycleEnd, currentBalance);
        result.put("timeline", timeline);
        
        // 从时间线获取最后的余额作为发薪前余额（保证一致）
        BigDecimal beforeSalary;
        if (timeline.isEmpty()) {
            beforeSalary = currentBalance;
        } else {
            // 取时间线最后一项的余额
            Map<String, Object> lastItem = timeline.get(timeline.size() - 1);
            beforeSalary = (BigDecimal) lastItem.get("balanceAfter");
        }
        result.put("beforeSalaryBalance", beforeSalary);
        
        // 计算发薪后余额
        BigDecimal afterSalary = beforeSalary.add(new BigDecimal(salaryAmount));
        result.put("afterSalaryBalance", afterSalary);
        
        // 判断是否够还
        result.put("isSufficient", beforeSalary.compareTo(BigDecimal.ZERO) >= 0);
        
        return result;
    }

    /**
     * 生成时间线（合并所有收支，按日期排序，计算每步后的余额）
     */
    private List<Map<String, Object>> generateTimeline(LocalDate startDate, LocalDate endDate, BigDecimal startBalance) {
        List<Map<String, Object>> timeline = new ArrayList<>();
        
        // 收集所有事件（已经是从startDate开始的了）
        List<Map<String, Object>> events = new ArrayList<>();
        
        // 添加贷款还款事件
        for (Map<String, Object> payment : getLoanPaymentDetails(startDate, endDate)) {
            Map<String, Object> event = new HashMap<>();
            event.put("date", payment.get("date"));
            event.put("type", "loan");
            event.put("description", payment.get("loanName"));
            event.put("platform", payment.get("platform"));
            event.put("amount", payment.get("amount"));
            event.put("isIncome", false);
            events.add(event);
        }
        
        // 添加固定支出事件
        for (Map<String, Object> expense : getFixedExpenseDetails(startDate, endDate)) {
            Map<String, Object> event = new HashMap<>();
            event.put("date", expense.get("date"));
            event.put("type", "expense");
            event.put("description", expense.get("expenseName"));
            event.put("amount", expense.get("amount"));
            event.put("isIncome", false);
            events.add(event);
        }
        
        // 添加临时收支事件
        List<com.finance.loans.model.TempTransaction> tempTrans = 
            tempTransactionService.getByDateRange(startDate, endDate);
        for (com.finance.loans.model.TempTransaction trans : tempTrans) {
            Map<String, Object> event = new HashMap<>();
            event.put("date", trans.getTransactionDate());
            event.put("type", trans.getType());
            event.put("description", trans.getDescription());
            event.put("amount", trans.getAmount());
            event.put("isIncome", "income".equals(trans.getType()));
            events.add(event);
        }
        
        // 按日期排序
        events.sort((a, b) -> ((LocalDate) a.get("date")).compareTo((LocalDate) b.get("date")));
        
        // 计算每步后的余额
        BigDecimal balance = startBalance;
        for (Map<String, Object> event : events) {
            Map<String, Object> timelineItem = new HashMap<>(event);
            timelineItem.put("balanceBefore", balance);
            
            BigDecimal amount = (BigDecimal) event.get("amount");
            if ((Boolean) event.get("isIncome")) {
                balance = balance.add(amount);
            } else {
                balance = balance.subtract(amount);
            }
            
            timelineItem.put("balanceAfter", balance);
            timeline.add(timelineItem);
        }
        
        return timeline;
    }

    /**
     * 计算周期内的贷款还款（含明细）
     */
    private BigDecimal calculateLoanPayments(LocalDate startDate, LocalDate endDate) {
        List<Loan> activeLoans = loanService.getActiveLoans();
        BigDecimal total = BigDecimal.ZERO;
        
        for (Loan loan : activeLoans) {
            LocalDate currentDate = startDate;
            while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
                LocalDate paymentDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), loan.getPaymentDay());
                
                if ((paymentDate.isAfter(startDate) || paymentDate.isEqual(startDate)) && 
                    paymentDate.isBefore(endDate)) {
                    total = total.add(loan.getMonthlyPayment());
                }
                
                currentDate = currentDate.plusMonths(1);
            }
        }
        
        return total;
    }

    /**
     * 获取周期内的贷款还款明细
     */
    public List<Map<String, Object>> getLoanPaymentDetails(LocalDate startDate, LocalDate endDate) {
        List<Loan> activeLoans = loanService.getActiveLoans();
        List<Map<String, Object>> details = new ArrayList<>();
        
        System.out.println("===== 调试: getLoanPaymentDetails =====");
        System.out.println("日期范围: " + startDate + " ~ " + endDate);
        System.out.println("活跃贷款数: " + activeLoans.size());
        
        for (Loan loan : activeLoans) {
            System.out.println("处理贷款: " + loan.getLoanName() + ", 还款日: " + loan.getPaymentDay());
            
            // 从开始月份到结束月份，检查每个月
            LocalDate checkMonth = startDate.withDayOfMonth(1);
            LocalDate endMonth = endDate.withDayOfMonth(1);
            
            while (!checkMonth.isAfter(endMonth)) {
                try {
                    LocalDate paymentDate = LocalDate.of(checkMonth.getYear(), checkMonth.getMonth(), loan.getPaymentDay());
                    
                    System.out.println("  检查日期: " + paymentDate + ", 是否在范围: " + 
                        (!paymentDate.isBefore(startDate) && paymentDate.isBefore(endDate)));
                    
                    // 判断还款日期是否在范围内
                    if (!paymentDate.isBefore(startDate) && paymentDate.isBefore(endDate)) {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("date", paymentDate);
                        detail.put("loanName", loan.getLoanName());
                        detail.put("platform", loan.getPlatform());
                        detail.put("amount", loan.getMonthlyPayment());
                        details.add(detail);
                        System.out.println("  ✓ 添加了还款: " + paymentDate);
                    }
                } catch (Exception e) {
                    System.out.println("  日期无效: " + e.getMessage());
                }
                
                checkMonth = checkMonth.plusMonths(1);
            }
        }
        
        // 按日期排序
        details.sort((a, b) -> ((LocalDate) a.get("date")).compareTo((LocalDate) b.get("date")));
        
        System.out.println("最终明细数量: " + details.size());
        System.out.println("===============================");
        
        return details;
    }

    /**
     * 获取周期内的固定支出明细
     */
    public List<Map<String, Object>> getFixedExpenseDetails(LocalDate startDate, LocalDate endDate) {
        List<Expense> activeExpenses = expenseService.getActiveExpenses();
        List<Map<String, Object>> details = new ArrayList<>();
        
        for (Expense expense : activeExpenses) {
            // 从开始月份到结束月份，检查每个月
            LocalDate checkMonth = startDate.withDayOfMonth(1);
            LocalDate endMonth = endDate.withDayOfMonth(1);
            
            while (!checkMonth.isAfter(endMonth)) {
                try {
                    LocalDate expenseDate = LocalDate.of(checkMonth.getYear(), checkMonth.getMonth(), expense.getExpenseDay());
                    
                    // 判断支出日期是否在范围内
                    if (!expenseDate.isBefore(startDate) && expenseDate.isBefore(endDate)) {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("date", expenseDate);
                        detail.put("expenseName", expense.getExpenseName());
                        detail.put("amount", expense.getAmount());
                        details.add(detail);
                    }
                } catch (Exception e) {
                    // 日期无效，跳过
                }
                
                checkMonth = checkMonth.plusMonths(1);
            }
        }
        
        // 按日期排序
        details.sort((a, b) -> ((LocalDate) a.get("date")).compareTo((LocalDate) b.get("date")));
        
        return details;
    }

    /**
     * 计算周期内的固定支出
     */
    private BigDecimal calculateFixedExpenses(LocalDate startDate, LocalDate endDate) {
        return calculateFixedExpensesInRange(startDate, endDate);
    }

    /**
     * 计算指定日期范围内的贷款还款
     */
    private BigDecimal calculateLoanPaymentsInRange(LocalDate startDate, LocalDate endDate) {
        List<Loan> activeLoans = loanService.getActiveLoans();
        BigDecimal total = BigDecimal.ZERO;
        
        for (Loan loan : activeLoans) {
            LocalDate currentDate = startDate;
            while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
                LocalDate paymentDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), loan.getPaymentDay());
                
                if ((paymentDate.isAfter(startDate) || paymentDate.isEqual(startDate)) && 
                    paymentDate.isBefore(endDate)) {
                    total = total.add(loan.getMonthlyPayment());
                }
                
                currentDate = currentDate.plusMonths(1);
            }
        }
        
        return total;
    }

    /**
     * 计算指定日期范围内的固定支出
     */
    private BigDecimal calculateFixedExpensesInRange(LocalDate startDate, LocalDate endDate) {
        List<Expense> activeExpenses = expenseService.getActiveExpenses();
        BigDecimal total = BigDecimal.ZERO;
        
        for (Expense expense : activeExpenses) {
            LocalDate currentDate = startDate;
            while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
                LocalDate expenseDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), expense.getExpenseDay());
                
                if ((expenseDate.isAfter(startDate) || expenseDate.isEqual(startDate)) && 
                    expenseDate.isBefore(endDate)) {
                    total = total.add(expense.getAmount());
                }
                
                currentDate = currentDate.plusMonths(1);
            }
        }
        
        return total;
    }

    /**
     * 获取未来N个周期的预测
     */
    public List<Map<String, Object>> getFutureCycles(int count) {
        List<Map<String, Object>> cycles = new ArrayList<>();
        
        int salaryDay = configService.getSalaryDay();
        LocalDate today = LocalDate.now();
        LocalDate currentCycleStart;
        
        if (today.getDayOfMonth() >= salaryDay) {
            currentCycleStart = LocalDate.of(today.getYear(), today.getMonth(), salaryDay);
        } else {
            currentCycleStart = LocalDate.of(today.getYear(), today.getMonth(), salaryDay).minusMonths(1);
        }
        
        for (int i = 0; i < count; i++) {
            LocalDate cycleStart = currentCycleStart.plusMonths(i);
            LocalDate cycleEnd = cycleStart.plusMonths(1);
            cycles.add(calculateCycle(cycleStart, cycleEnd, i == 0));
        }
        
        return cycles;
    }
}

