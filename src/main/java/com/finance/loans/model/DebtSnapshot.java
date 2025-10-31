package com.finance.loans.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 债务快照模型（每日记录）
 */
@Data
public class DebtSnapshot {
    
    private Long id;
    private LocalDate snapshotDate;
    private BigDecimal totalDebt;
    private BigDecimal totalMonthlyPayment;
    private Integer activeLoanCount;
    private LocalDateTime createdAt;
}

