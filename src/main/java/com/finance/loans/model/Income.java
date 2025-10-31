package com.finance.loans.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收入实体类
 */
@Data
public class Income {

    private Long id;
    private String incomeType;
    private BigDecimal amount;
    private Integer incomeDay;
    private String description;
    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

