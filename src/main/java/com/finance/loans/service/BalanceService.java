package com.finance.loans.service;

import com.finance.loans.mapper.BalanceHistoryMapper;
import com.finance.loans.model.BalanceHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

/**
 * 余额业务逻辑服务
 */
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceHistoryMapper balanceHistoryMapper;

    /**
     * 获取当前余额
     */
    public BigDecimal getCurrentBalance() {
        BalanceHistory latest = balanceHistoryMapper.findLatest();
        return latest != null ? latest.getBalance() : BigDecimal.ZERO;
    }

    /**
     * 获取余额历史记录
     */
    public List<BalanceHistory> getBalanceHistory() {
        return balanceHistoryMapper.findAll();
    }

    /**
     * 获取最新的余额记录
     */
    public BalanceHistory getLatestBalanceRecord() {
        return balanceHistoryMapper.findLatest();
    }

    /**
     * 更新余额（手动更新）
     */
    @Transactional
    public BalanceHistory updateBalance(BigDecimal newBalance, String description) {
        BigDecimal currentBalance = getCurrentBalance();
        BigDecimal change = newBalance.subtract(currentBalance);
        
        BalanceHistory history = BalanceHistory.create(
            newBalance, change, "manual", null, description
        );
        
        balanceHistoryMapper.insert(history);
        return history;
    }

    /**
     * 增加余额（收入）
     */
    @Transactional
    public BalanceHistory addBalance(BigDecimal amount, String changeType, 
                                    Long relatedId, String description) {
        BigDecimal currentBalance = getCurrentBalance();
        BigDecimal newBalance = currentBalance.add(amount);
        
        BalanceHistory history = BalanceHistory.create(
            newBalance, amount, changeType, relatedId, description
        );
        
        balanceHistoryMapper.insert(history);
        return history;
    }

    /**
     * 扣减余额（支出/还款）
     */
    @Transactional
    public BalanceHistory deductBalance(BigDecimal amount, String changeType, 
                                       Long relatedId, String description) {
        BigDecimal currentBalance = getCurrentBalance();
        BigDecimal newBalance = currentBalance.subtract(amount);
        
        BalanceHistory history = BalanceHistory.create(
            newBalance, amount.negate(), changeType, relatedId, description
        );
        
        balanceHistoryMapper.insert(history);
        return history;
    }

    /**
     * 按类型查询余额变动
     */
    public List<BalanceHistory> getBalanceHistoryByType(String changeType) {
        return balanceHistoryMapper.findByChangeType(changeType);
    }
}

