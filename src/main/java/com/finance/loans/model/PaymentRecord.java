package com.finance.loans.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 还款记录实体类
 */
@Data
public class PaymentRecord {
    
    /** 主键ID */
    private Long id;
    
    /** 贷款ID */
    private Long loanId;
    
    /** 关联的计划ID(正常还款时关联) */
    private Long scheduleId;
    
    /** 还款金额 */
    private BigDecimal amount;
    
    /** 还款日期 */
    private LocalDate paymentDate;
    
    /** 还款类型: normal-正常还款, early-提前还款, extra-额外还款 */
    private String paymentType;
    
    /** 备注 */
    private String note;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
