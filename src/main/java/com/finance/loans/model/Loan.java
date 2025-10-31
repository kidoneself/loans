package com.finance.loans.model;

import lombok.Data;
import java.math.BigDecimal;
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
     * 计算预计还清日期
     */
    public LocalDate calculatePayoffDate() {
        if (monthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        int remainingMonths = remainingAmount.divide(monthlyPayment, 0, BigDecimal.ROUND_UP).intValue();
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
        return repaid.divide(totalAmount, 4, BigDecimal.ROUND_HALF_UP)
                     .multiply(new BigDecimal("100"))
                     .doubleValue();
    }
}

