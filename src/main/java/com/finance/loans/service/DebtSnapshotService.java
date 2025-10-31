package com.finance.loans.service;

import com.finance.loans.model.DebtSnapshot;
import com.finance.loans.model.Loan;
import com.finance.loans.mapper.DebtSnapshotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 债务快照服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DebtSnapshotService {
    
    private final DebtSnapshotMapper snapshotMapper;
    private final LoanService loanService;
    
    /**
     * 每天凌晨2点自动记录快照
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void createDailySnapshot() {
        LocalDate today = LocalDate.now();
        createSnapshot(today);
        log.info("已创建债务快照: {}", today);
    }
    
    /**
     * 手动创建快照（可用于补充历史数据）
     */
    @Transactional
    public DebtSnapshot createSnapshot(LocalDate date) {
        // 计算当前债务数据
        List<Loan> activeLoans = loanService.getActiveLoans();
        
        BigDecimal totalDebt = activeLoans.stream()
                .map(Loan::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalMonthlyPayment = activeLoans.stream()
                .map(Loan::getMonthlyPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 创建或更新快照（使用 ON DUPLICATE KEY UPDATE）
        DebtSnapshot snapshot = new DebtSnapshot();
        snapshot.setSnapshotDate(date);
        snapshot.setTotalDebt(totalDebt);
        snapshot.setTotalMonthlyPayment(totalMonthlyPayment);
        snapshot.setActiveLoanCount(activeLoans.size());
        
        snapshotMapper.insertOrUpdate(snapshot);
        
        // 返回创建的快照
        return snapshotMapper.findBySnapshotDate(date);
    }
    
    /**
     * 获取日趋势数据
     */
    public List<Map<String, Object>> getDailyTrend(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        List<DebtSnapshot> snapshots = snapshotMapper.findByDateRange(startDate, endDate);
        
        return snapshots.stream().map(s -> {
            Map<String, Object> data = new HashMap<>();
            data.put("date", s.getSnapshotDate().toString());
            data.put("dateDisplay", s.getSnapshotDate().toString());
            data.put("totalDebt", s.getTotalDebt());
            data.put("monthlyPayment", s.getTotalMonthlyPayment());
            data.put("loanCount", s.getActiveLoanCount());
            return data;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取月趋势数据（每月最后一天的快照）
     */
    public List<Map<String, Object>> getMonthlyTrend(int months) {
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        
        YearMonth currentMonth = YearMonth.now();
        
        for (int i = months - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            
            // 查找该月最后一天的快照
            LocalDate lastDayOfMonth = month.atEndOfMonth();
            
            // 如果是当前月，使用今天
            if (month.equals(YearMonth.now())) {
                lastDayOfMonth = LocalDate.now();
            }
            
            DebtSnapshot snapshot = findClosestSnapshot(lastDayOfMonth);
            
            Map<String, Object> data = new HashMap<>();
            data.put("month", month.toString());
            data.put("monthDisplay", month.getYear() + "年" + month.getMonthValue() + "月");
            
            if (snapshot != null) {
                data.put("totalDebt", snapshot.getTotalDebt());
                data.put("monthlyPayment", snapshot.getTotalMonthlyPayment());
                data.put("loanCount", snapshot.getActiveLoanCount());
                data.put("hasData", true);
            } else {
                data.put("totalDebt", BigDecimal.ZERO);
                data.put("monthlyPayment", BigDecimal.ZERO);
                data.put("loanCount", 0);
                data.put("hasData", false);
            }
            
            monthlyData.add(data);
        }
        
        return monthlyData;
    }
    
    /**
     * 查找最接近指定日期的快照
     */
    private DebtSnapshot findClosestSnapshot(LocalDate targetDate) {
        // 先尝试查找当天
        DebtSnapshot snapshot = snapshotMapper.findBySnapshotDate(targetDate);
        if (snapshot != null) {
            return snapshot;
        }
        
        // 向前查找7天内的快照
        for (int i = 1; i <= 7; i++) {
            snapshot = snapshotMapper.findBySnapshotDate(targetDate.minusDays(i));
            if (snapshot != null) {
                return snapshot;
            }
        }
        
        return null;
    }
    
    /**
     * 获取统计数据
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取最新快照
        DebtSnapshot latest = snapshotMapper.findLatest();
        
        if (latest != null) {
            stats.put("currentDebt", latest.getTotalDebt());
            stats.put("currentDate", latest.getSnapshotDate().toString());
            
            // 获取30天前的快照
            LocalDate thirtyDaysAgo = latest.getSnapshotDate().minusDays(30);
            DebtSnapshot past = findClosestSnapshot(thirtyDaysAgo);
            
            if (past != null) {
                BigDecimal decrease = past.getTotalDebt().subtract(latest.getTotalDebt());
                stats.put("thirtyDayDecrease", decrease);
                stats.put("hasDecrease", decrease.compareTo(BigDecimal.ZERO) > 0);
            } else {
                stats.put("thirtyDayDecrease", BigDecimal.ZERO);
                stats.put("hasDecrease", false);
            }
        } else {
            stats.put("currentDebt", BigDecimal.ZERO);
            stats.put("thirtyDayDecrease", BigDecimal.ZERO);
            stats.put("hasDecrease", false);
        }
        
        return stats;
    }
}

