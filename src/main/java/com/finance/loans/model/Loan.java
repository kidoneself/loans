package com.finance.loans.model;

import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 贷款实体类
 */
@Data
public class Loan {

    private Long id;
    private String loanName;
    private String platform;
    private BigDecimal totalAmount;
    private BigDecimal remainingAmount;
    private BigDecimal monthlyPayment;
    private Integer paymentDay;
    private Integer totalPeriods;
    private Integer paidPeriods = 0;
    private LocalDate startDate;
    private String status = "active";
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 计算预计还清日期（基于还款日计算）
     */
    public LocalDate calculatePayoffDate() {
        if (monthlyPayment == null || monthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        
        // 优先使用期数计算（更准确）
        if (totalPeriods != null && paidPeriods != null && paymentDay != null) {
            int remainingPeriods = totalPeriods - paidPeriods;
            if (remainingPeriods <= 0) {
                return null; // 已还清
            }
            
            LocalDate today = LocalDate.now();
            int currentDay = today.getDayOfMonth();
            
            // 如果今天还没到本月还款日，最后一期在：当前月份 + (剩余期数-1) 个月
            // 如果今天已过本月还款日，最后一期在：下个月 + (剩余期数-1) 个月
            LocalDate baseMonth = currentDay <= paymentDay ? today : today.plusMonths(1);
            
            // 计算最后还款月份
            LocalDate payoffMonth = baseMonth.plusMonths(remainingPeriods - 1);
            
            // 设置为还款日（处理月末情况）
            int lastDayOfMonth = payoffMonth.lengthOfMonth();
            int actualDay = Math.min(paymentDay, lastDayOfMonth);
            
            return payoffMonth.withDayOfMonth(actualDay);
        }
        
        // 降级方案：根据金额计算（不太准确）
        int remainingMonths = remainingAmount.divide(monthlyPayment, 0, java.math.RoundingMode.UP).intValue();
        if (remainingMonths <= 0) {
            return null;
        }
        
        // 如果有还款日，计算到具体日期
        if (paymentDay != null) {
            LocalDate today = LocalDate.now();
            int currentDay = today.getDayOfMonth();
            LocalDate baseMonth = currentDay <= paymentDay ? today : today.plusMonths(1);
            LocalDate payoffMonth = baseMonth.plusMonths(remainingMonths - 1);
            int lastDayOfMonth = payoffMonth.lengthOfMonth();
            int actualDay = Math.min(paymentDay, lastDayOfMonth);
            return payoffMonth.withDayOfMonth(actualDay);
        }
        
        // 最后的降级方案
        return LocalDate.now().plusMonths(remainingMonths);
    }

    /**
     * 计算已还比例
     */
    public double getRepaidPercentage() {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        BigDecimal repaid = totalAmount.subtract(remainingAmount);
        return repaid.divide(totalAmount, 4, RoundingMode.HALF_UP)
                     .multiply(new BigDecimal("100"))
                     .doubleValue();
    }
}

