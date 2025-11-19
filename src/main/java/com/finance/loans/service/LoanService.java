package com.finance.loans.service;

import com.finance.loans.mapper.LoanMapper;
import com.finance.loans.mapper.RepaymentScheduleMapper;
import com.finance.loans.model.Loan;
import com.finance.loans.model.RepaymentSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 贷款服务
 */
@Service
public class LoanService {
    
    @Autowired
    private LoanMapper loanMapper;
    
    @Autowired
    private RepaymentScheduleMapper scheduleMapper;
    
    /**
     * 查询所有贷款
     */
    public List<Loan> getAllLoans() {
        return loanMapper.findAll();
    }
    
    /**
     * 查询活跃贷款
     */
    public List<Loan> getActiveLoans() {
        return loanMapper.findByStatus("active");
    }
    
    /**
     * 根据ID查询贷款
     */
    public Loan getLoanById(Long id) {
        return loanMapper.findById(id);
    }
    
    /**
     * 添加贷款并自动生成还款计划
     */
    @Transactional
    public Loan addLoan(Loan loan) {
        if (loan.getStatus() == null) {
            loan.setStatus("active");
        }
        
        // 插入贷款
        loanMapper.insert(loan);
        
        // 自动生成还款计划
        if (loan.getTotalPeriods() != null && loan.getTotalPeriods() > 0 
            && loan.getMonthlyAmount() != null && loan.getPaymentDay() != null 
            && loan.getStartDate() != null) {
            generateRepaymentSchedule(loan);
        }
        
        return loan;
    }
    
    /**
     * 更新贷款
     */
    @Transactional
    public Loan updateLoan(Long id, Loan loanDetails) {
        Loan loan = getLoanById(id);
        if (loan == null) {
            throw new RuntimeException("贷款不存在");
        }
        
        // 检查关键字段是否变化（需要重新生成计划）
        boolean needRegeneratePlan = false;
        if (!loan.getTotalPeriods().equals(loanDetails.getTotalPeriods())
            || !loan.getMonthlyAmount().equals(loanDetails.getMonthlyAmount())
            || !loan.getPaymentDay().equals(loanDetails.getPaymentDay())
            || !loan.getStartDate().equals(loanDetails.getStartDate())) {
            needRegeneratePlan = true;
        }
        
        // 更新字段
        loan.setName(loanDetails.getName());
        loan.setPlatform(loanDetails.getPlatform());
        loan.setPrincipal(loanDetails.getPrincipal());
        loan.setMonthlyAmount(loanDetails.getMonthlyAmount());
        loan.setTotalPeriods(loanDetails.getTotalPeriods());
        loan.setPaymentDay(loanDetails.getPaymentDay());
        loan.setStartDate(loanDetails.getStartDate());
        loan.setStatus(loanDetails.getStatus());
        loan.setNote(loanDetails.getNote());
        
        loanMapper.update(loan);
        
        // 如果关键字段变化，重新生成还款计划
        if (needRegeneratePlan) {
            scheduleMapper.deleteByLoanId(loan.getId());
            generateRepaymentSchedule(loan);
        }
        
        return loan;
    }
    
    /**
     * 删除贷款
     */
    @Transactional
    public void deleteLoan(Long id) {
        scheduleMapper.deleteByLoanId(id);
        loanMapper.deleteById(id);
    }
    
    /**
     * 提前还清贷款
     */
    @Transactional
    public void markAsEarlySettlement(Long id) {
        Loan loan = getLoanById(id);
        if (loan == null) {
            throw new RuntimeException("贷款不存在");
        }
        
        // 获取所有未还的计划
        List<RepaymentSchedule> pendingSchedules = scheduleMapper.findByLoanIdAndStatus(id, "pending");
        
        // 标记所有未还的为已还
        if (!pendingSchedules.isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (RepaymentSchedule schedule : pendingSchedules) {
                ids.add(schedule.getId());
            }
            scheduleMapper.batchUpdateStatus(ids, "paid", LocalDate.now());
        }
        
        // 更新贷款状态为已结清
        loan.setStatus("completed");
        loanMapper.update(loan);
    }
    
    /**
     * 生成还款计划
     */
    private void generateRepaymentSchedule(Loan loan) {
        List<RepaymentSchedule> schedules = new ArrayList<>();
        LocalDate startDate = loan.getStartDate();
        
        for (int period = 1; period <= loan.getTotalPeriods(); period++) {
            RepaymentSchedule schedule = new RepaymentSchedule();
            schedule.setLoanId(loan.getId());
            schedule.setPeriod(period);
            
            // 计算应还日期
            LocalDate dueDate = startDate.plusMonths(period - 1);
            // 调整为指定还款日
            dueDate = dueDate.withDayOfMonth(Math.min(loan.getPaymentDay(), dueDate.lengthOfMonth()));
            
            schedule.setDueDate(dueDate);
            schedule.setAmount(loan.getMonthlyAmount());
            
            // 判断状态
            if (dueDate.isBefore(LocalDate.now())) {
                schedule.setStatus("paid");
                schedule.setPaidDate(dueDate);
                schedule.setPaidAmount(loan.getMonthlyAmount());
            } else {
                schedule.setStatus("pending");
            }
            
            schedules.add(schedule);
        }
        
        // 批量插入
        if (!schedules.isEmpty()) {
            scheduleMapper.batchInsert(schedules);
        }
    }
    
    /**
     * 获取贷款统计
     */
    public Map<String, Object> getLoanSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        List<Loan> activeLoans = getActiveLoans();
        
        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal monthlyPayment = BigDecimal.ZERO;
        int activeCount = activeLoans.size();
        int completedCount = loanMapper.countByStatus("completed");
        
        for (Loan loan : activeLoans) {
            totalPrincipal = totalPrincipal.add(loan.getPrincipal() != null ? loan.getPrincipal() : BigDecimal.ZERO);
            monthlyPayment = monthlyPayment.add(loan.getMonthlyAmount() != null ? loan.getMonthlyAmount() : BigDecimal.ZERO);
        }
        
        // 计算剩余负债
        BigDecimal remainingDebt = BigDecimal.ZERO;
        for (Loan loan : activeLoans) {
            List<RepaymentSchedule> schedules = scheduleMapper.findByLoanIdAndStatus(loan.getId(), "pending");
            for (RepaymentSchedule schedule : schedules) {
                remainingDebt = remainingDebt.add(schedule.getAmount());
            }
        }
        
        summary.put("totalPrincipal", totalPrincipal);
        summary.put("remainingDebt", remainingDebt);
        summary.put("monthlyPayment", monthlyPayment);
        summary.put("activeLoans", activeCount);
        summary.put("completedLoans", completedCount);
        
        return summary;
    }
}
