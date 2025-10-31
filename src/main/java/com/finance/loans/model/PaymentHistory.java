package com.finance.loans.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 还款历史实体类
 */
@Data
public class PaymentHistory {

    private Long id;
    private Long loanId;
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;
    private Boolean isExtraPayment = false;
    private Boolean autoDeductBalance = true;
    private String note;
    private LocalDateTime createdAt;
}

