package com.finance.loans.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 还款计划实体类
 */
@Data
public class RepaymentSchedule {
    
    /** 主键ID */
    private Long id;
    
    /** 贷款ID */
    private Long loanId;
    
    /** 期数(第几期) */
    private Integer period;
    
    /** 应还日期 */
    private LocalDate dueDate;
    
    /** 应还金额 */
    private BigDecimal amount;
    
    /** 状态: pending-待还, paid-已还, overdue-逾期 */
    private String status;
    
    /** 实际还款日期 */
    private LocalDate paidDate;
    
    /** 实际还款金额 */
    private BigDecimal paidAmount;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
