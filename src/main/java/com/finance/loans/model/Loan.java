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
    
    /** 主键ID */
    private Long id;
    
    /** 贷款名称 */
    private String name;
    
    /** 贷款平台 */
    private String platform;
    
    /** 借款本金 */
    private BigDecimal principal;
    
    /** 月还款额 */
    private BigDecimal monthlyAmount;
    
    /** 总期数 */
    private Integer totalPeriods;
    
    /** 每月还款日(1-31) */
    private Integer paymentDay;
    
    /** 首期还款日期 */
    private LocalDate startDate;
    
    /** 状态: active-还款中, completed-已结清 */
    private String status;
    
    /** 备注信息 */
    private String note;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
