package com.finance.loans.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 余额历史实体类
 */
@Data
public class BalanceHistory {

    private Long id;
    private BigDecimal balance;
    private BigDecimal changeAmount = BigDecimal.ZERO;
    private String changeType;
    private Long relatedId;
    private String description;
    private LocalDateTime createdAt;

    /**
     * 创建余额记录的工厂方法
     */
    public static BalanceHistory create(BigDecimal balance, BigDecimal changeAmount, 
                                        String changeType, Long relatedId, String description) {
        BalanceHistory history = new BalanceHistory();
        history.setBalance(balance);
        history.setChangeAmount(changeAmount);
        history.setChangeType(changeType);
        history.setRelatedId(relatedId);
        history.setDescription(description);
        return history;
    }
}

