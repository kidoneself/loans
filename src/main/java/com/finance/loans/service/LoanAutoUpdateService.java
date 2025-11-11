package com.finance.loans.service;

import com.finance.loans.mapper.LoanMapper;
import com.finance.loans.mapper.PaymentHistoryMapper;
import com.finance.loans.model.Loan;
import com.finance.loans.model.PaymentHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 贷款自动更新服务
 * 定时任务：自动更新已还期数
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanAutoUpdateService {

    private final LoanMapper loanMapper;
    private final PaymentHistoryMapper paymentHistoryMapper;
    
    @Value("${loan.auto-update.enabled:false}")
    private boolean autoUpdateEnabled;

    /**
     * 每天凌晨3点自动更新已还期数
     * 
     * 逻辑：
     * 1. 遍历所有活跃贷款
     * 2. 检查今天是否过了还款日
     * 3. 如果过了还款日且本月还没有自动更新过，则自动增加已还期数
     * 
     * 注意：此任务假设用户按时还款。如果某月未还款，可能需要手动调整。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void autoUpdatePaidPeriods() {
        // 检查是否启用自动更新
        if (!autoUpdateEnabled) {
            log.debug("自动更新已还期数功能已禁用");
            return;
        }
        
        LocalDate today = LocalDate.now();
        List<Loan> activeLoans = loanMapper.findByStatus("active");
        
        int updatedCount = 0;
        
        for (Loan loan : activeLoans) {
            try {
                // 基本检查
                if (loan.getPaymentDay() == null || loan.getTotalPeriods() == null || loan.getPaidPeriods() == null) {
                    continue;
                }
                
                // 如果已经还清了，跳过
                if (loan.getPaidPeriods() >= loan.getTotalPeriods()) {
                    continue;
                }
                
                // 获取还款日
                int paymentDay = loan.getPaymentDay();
                int currentDay = today.getDayOfMonth();
                
                // 如果今天刚好是还款日，或者已经过了还款日
                if (currentDay >= paymentDay) {
                    // 检查是否需要更新（避免重复更新）
                    // 通过 updatedAt 判断本月是否已经更新过
                    if (shouldUpdateThisMonth(loan, today)) {
                        // 增加已还期数
                        loan.setPaidPeriods(loan.getPaidPeriods() + 1);
                        
                        // 重新计算剩余金额
                        int remainingPeriods = loan.getTotalPeriods() - loan.getPaidPeriods();
                        if (loan.getMonthlyPayment() != null) {
                            loan.setRemainingAmount(
                                loan.getMonthlyPayment().multiply(new java.math.BigDecimal(remainingPeriods))
                            );
                        }
                        
                        // 如果还清了，更新状态
                        if (loan.getPaidPeriods() >= loan.getTotalPeriods()) {
                            loan.setStatus("completed");
                            loan.setRemainingAmount(java.math.BigDecimal.ZERO);
                            log.info("贷款 [{}] 已自动标记为已结清", loan.getLoanName());
                        }
                        
                        // 保存更新
                        loanMapper.update(loan);
                        
                        // 创建还款记录
                        PaymentHistory payment = new PaymentHistory();
                        payment.setLoanId(loan.getId());
                        payment.setPaymentAmount(loan.getMonthlyPayment());
                        payment.setPaymentDate(today);
                        payment.setAutoDeductBalance(false); // 自动记录不扣减余额
                        payment.setNote("系统自动记录");
                        paymentHistoryMapper.insert(payment);
                        
                        updatedCount++;
                        
                        log.info("已自动更新贷款 [{}] 的已还期数: {} -> {}，并创建还款记录", 
                            loan.getLoanName(), 
                            loan.getPaidPeriods() - 1, 
                            loan.getPaidPeriods());
                    }
                }
            } catch (Exception e) {
                log.error("自动更新贷款 [{}] 失败: {}", loan.getLoanName(), e.getMessage());
            }
        }
        
        if (updatedCount > 0) {
            log.info("定时任务完成：已自动更新 {} 笔贷款的还款期数", updatedCount);
        }
    }
    
    /**
     * 判断本月是否应该更新
     * 
     * 逻辑：如果 updatedAt 不在本月，则需要更新
     */
    private boolean shouldUpdateThisMonth(Loan loan, LocalDate today) {
        if (loan.getUpdatedAt() == null) {
            return true;
        }
        
        LocalDate lastUpdate = loan.getUpdatedAt().toLocalDate();
        
        // 如果最后更新时间不在本月，则需要更新
        return lastUpdate.getYear() != today.getYear() 
            || lastUpdate.getMonthValue() != today.getMonthValue();
    }
    
    /**
     * 手动触发更新（用于测试或手动执行）
     */
    @Transactional
    public int manualUpdatePaidPeriods() {
        log.info("手动触发贷款期数自动更新...");
        autoUpdatePaidPeriods();
        return 1;
    }
}

