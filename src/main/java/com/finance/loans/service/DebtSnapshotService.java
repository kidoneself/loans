package com.finance.loans.service;

import com.finance.loans.mapper.DebtSnapshotMapper;
import com.finance.loans.mapper.LoanMapper;
import com.finance.loans.mapper.RepaymentScheduleMapper;
import com.finance.loans.model.DebtSnapshot;
import com.finance.loans.model.Loan;
import com.finance.loans.model.RepaymentSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 负债快照服务
 */
@Service
public class DebtSnapshotService {
    
    @Autowired
    private DebtSnapshotMapper snapshotMapper;
    
    @Autowired
    private LoanMapper loanMapper;
    
    @Autowired
    private RepaymentScheduleMapper scheduleMapper;
    
    /**
     * 获取所有快照
     */
    public List<DebtSnapshot> getAllSnapshots() {
        return snapshotMapper.findAll();
    }
    
    /**
     * 获取最新快照
     */
    public DebtSnapshot getLatestSnapshot() {
        return snapshotMapper.findLatest();
    }
    
    /**
     * 获取指定日期范围的快照
     */
    public List<DebtSnapshot> getSnapshotsByDateRange(LocalDate startDate, LocalDate endDate) {
        return snapshotMapper.findByDateRange(startDate, endDate);
    }
    
    /**
     * 获取最近N天的快照
     */
    public List<DebtSnapshot> getRecentSnapshots(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        return snapshotMapper.findByDateRange(startDate, endDate);
    }
    
    /**
     * 创建每日快照（定时任务，每天凌晨执行）
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void createDailySnapshot() {
        createSnapshot(LocalDate.now(), "daily");
    }
    
    /**
     * 手动创建快照
     */
    @Transactional
    public DebtSnapshot createSnapshot(LocalDate date, String type) {
        // 检查是否已存在
        DebtSnapshot existing = snapshotMapper.findByDateAndType(date, type);
        if (existing != null) {
            return existing;
        }
        
        // 计算统计数据
        List<Loan> allLoans = loanMapper.findAll();
        List<Loan> activeLoans = loanMapper.findByStatus("active");
        
        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal monthlyPayment = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal remainingAmount = BigDecimal.ZERO;
        
        for (Loan loan : allLoans) {
            totalPrincipal = totalPrincipal.add(loan.getPrincipal() != null ? loan.getPrincipal() : BigDecimal.ZERO);
            
            // 计算已还金额
            List<RepaymentSchedule> paidSchedules = scheduleMapper.findByLoanIdAndStatus(loan.getId(), "paid");
            for (RepaymentSchedule schedule : paidSchedules) {
                paidAmount = paidAmount.add(schedule.getPaidAmount() != null ? schedule.getPaidAmount() : BigDecimal.ZERO);
            }
        }
        
        for (Loan loan : activeLoans) {
            monthlyPayment = monthlyPayment.add(loan.getMonthlyAmount() != null ? loan.getMonthlyAmount() : BigDecimal.ZERO);
            
            // 计算剩余金额
            List<RepaymentSchedule> pendingSchedules = scheduleMapper.findByLoanIdAndStatus(loan.getId(), "pending");
            for (RepaymentSchedule schedule : pendingSchedules) {
                remainingAmount = remainingAmount.add(schedule.getAmount());
            }
        }
        
        // 创建快照
        DebtSnapshot snapshot = new DebtSnapshot();
        snapshot.setSnapshotDate(date);
        snapshot.setTotalDebt(remainingAmount);
        snapshot.setTotalPrincipal(totalPrincipal);
        snapshot.setPaidAmount(paidAmount);
        snapshot.setRemainingAmount(remainingAmount);
        snapshot.setActiveLoans(activeLoans.size());
        snapshot.setCompletedLoans(loanMapper.countByStatus("completed"));
        snapshot.setMonthlyPayment(monthlyPayment);
        snapshot.setSnapshotType(type);
        
        snapshotMapper.insert(snapshot);
        
        return snapshot;
    }
}
