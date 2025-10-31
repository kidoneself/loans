package com.finance.loans.service;

import com.finance.loans.model.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 日历服务 - 处理日历相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final LoanService loanService;

    /**
     * 获取某月的还款统计
     */
    public Map<String, Object> getMonthSummary(int year, int month) {
        List<Loan> activeLoans = loanService.getActiveLoans();
        
        // 统计该月所有还款
        Map<Integer, List<Map<String, Object>>> dailyPayments = new HashMap<>();
        BigDecimal monthTotal = BigDecimal.ZERO;
        int paymentCount = 0;
        
        LocalDate targetMonth = LocalDate.of(year, month, 1);
        
        for (Loan loan : activeLoans) {
            // 检查这笔贷款在目标月份是否还在还款
            if (!isLoanActiveInMonth(loan, targetMonth)) {
                continue;  // 这笔贷款已经还清了，跳过
            }
            
            int paymentDay = loan.getPaymentDay();
            
            // 检查这个日期是否在当月有效
            try {
                LocalDate paymentDate = LocalDate.of(year, month, paymentDay);
                
                if (!dailyPayments.containsKey(paymentDay)) {
                    dailyPayments.put(paymentDay, new ArrayList<>());
                }
                
                Map<String, Object> payment = new HashMap<>();
                payment.put("loanId", loan.getId());
                payment.put("loanName", loan.getLoanName());
                payment.put("platform", loan.getPlatform());
                payment.put("amount", loan.getMonthlyPayment());
                
                dailyPayments.get(paymentDay).add(payment);
                monthTotal = monthTotal.add(loan.getMonthlyPayment());
                paymentCount++;
            } catch (Exception e) {
                // 日期无效（比如2月30号），跳过
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("dailyPayments", dailyPayments);
        result.put("monthTotal", monthTotal);
        result.put("monthCount", paymentCount);
        
        return result;
    }

    /**
     * 判断贷款在某月是否还在还款
     */
    private boolean isLoanActiveInMonth(Loan loan, LocalDate targetMonth) {
        // 如果没有总期数和已还期数信息，只检查当前月份
        if (loan.getTotalPeriods() == null || loan.getPaidPeriods() == null) {
            // 只显示当前月份前后1个月的数据
            LocalDate today = LocalDate.now();
            LocalDate minMonth = today.minusMonths(1).withDayOfMonth(1);
            LocalDate maxMonth = today.plusMonths(2).withDayOfMonth(1);
            return !targetMonth.isBefore(minMonth) && targetMonth.isBefore(maxMonth);
        }
        
        LocalDate today = LocalDate.now();
        int totalPeriods = loan.getTotalPeriods();
        int paidPeriods = loan.getPaidPeriods();
        int remainingPeriods = totalPeriods - paidPeriods;
        
        if (remainingPeriods <= 0) {
            return false;  // 已经还完了
        }
        
        // 计算开始还款日期（如果有startDate就用，否则根据已还期数推算）
        LocalDate startPaymentMonth;
        if (loan.getStartDate() != null) {
            startPaymentMonth = loan.getStartDate().withDayOfMonth(1);
        } else {
            // 推算：今天往前推已还期数
            startPaymentMonth = today.minusMonths(paidPeriods).withDayOfMonth(1);
        }
        
        // 计算最后还款月份
        LocalDate endPaymentMonth = today.plusMonths(remainingPeriods).withDayOfMonth(1);
        
        // 目标月份必须在开始和结束之间
        LocalDate targetMonthFirst = targetMonth.withDayOfMonth(1);
        return !targetMonthFirst.isBefore(startPaymentMonth) && 
               targetMonthFirst.isBefore(endPaymentMonth.plusMonths(1));
    }

    /**
     * 获取某天的还款列表
     */
    public List<Map<String, Object>> getDayPayments(int year, int month, int day) {
        List<Loan> activeLoans = loanService.getActiveLoans();
        List<Map<String, Object>> payments = new ArrayList<>();
        
        LocalDate targetMonth = LocalDate.of(year, month, 1);
        
        for (Loan loan : activeLoans) {
            // 检查这笔贷款在目标月份是否还在还款
            if (!isLoanActiveInMonth(loan, targetMonth)) {
                continue;
            }
            
            if (loan.getPaymentDay() == day) {
                Map<String, Object> payment = new HashMap<>();
                payment.put("loanId", loan.getId());
                payment.put("loanName", loan.getLoanName());
                payment.put("platform", loan.getPlatform());
                payment.put("amount", loan.getMonthlyPayment());
                payments.add(payment);
            }
        }
        
        return payments;
    }
}

