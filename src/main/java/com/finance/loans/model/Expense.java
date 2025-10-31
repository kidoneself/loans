package com.finance.loans.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 固定支出实体类
 */
@Data
public class Expense {

    private Long id;
    private String expenseName;
    private BigDecimal amount;
    private Integer expenseDay;
    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

