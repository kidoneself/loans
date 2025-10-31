package com.finance.loans.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 临时收支实体类
 */
@Data
public class TempTransaction {
    private Long id;
    private LocalDate transactionDate;
    private String type; // income-收入, expense-支出
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
}

