package com.finance.loans.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 负债快照实体类
 */
@Data
public class DebtSnapshot {
    
    /** 主键ID */
    private Long id;
    
    /** 快照日期 */
    private LocalDate snapshotDate;
    
    /** 总负债(所有未还金额) */
    private BigDecimal totalDebt;
    
    /** 总本金 */
    private BigDecimal totalPrincipal;
    
    /** 已还总额 */
    private BigDecimal paidAmount;
    
    /** 剩余总额 */
    private BigDecimal remainingAmount;
    
    /** 活跃贷款数量 */
    private Integer activeLoans;
    
    /** 已结清贷款数量 */
    private Integer completedLoans;
    
    /** 月还款总额 */
    private BigDecimal monthlyPayment;
    
    /** 快照类型: daily-每日, monthly-每月 */
    private String snapshotType;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
