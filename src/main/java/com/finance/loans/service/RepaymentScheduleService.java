package com.finance.loans.service;

import com.finance.loans.mapper.RepaymentScheduleMapper;
import com.finance.loans.mapper.PaymentRecordMapper;
import com.finance.loans.model.RepaymentSchedule;
import com.finance.loans.model.PaymentRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 还款计划服务
 */
@Service
public class RepaymentScheduleService {
    
    @Autowired
    private RepaymentScheduleMapper scheduleMapper;
    
    @Autowired
    private PaymentRecordMapper recordMapper;
    
    /**
     * 获取贷款的还款计划
     */
    public List<RepaymentSchedule> getLoanSchedules(Long loanId) {
        return scheduleMapper.findByLoanId(loanId);
    }
    
    /**
     * 获取待还计划
     */
    public List<RepaymentSchedule> getPendingSchedules(Long loanId) {
        return scheduleMapper.findByLoanIdAndStatus(loanId, "pending");
    }
    
    /**
     * 获取本月还款计划
     */
    public List<RepaymentSchedule> getCurrentMonthSchedules() {
        LocalDate now = LocalDate.now();
        return scheduleMapper.findByMonth(now.getYear(), now.getMonthValue());
    }
    
    /**
     * 获取指定月份的还款计划
     */
    public List<RepaymentSchedule> getMonthSchedules(int year, int month) {
        return scheduleMapper.findByMonth(year, month);
    }
    
    /**
     * 记录还款
     */
    @Transactional
    public void recordPayment(Long scheduleId, BigDecimal amount, LocalDate paymentDate) {
        RepaymentSchedule schedule = scheduleMapper.findById(scheduleId);
        if (schedule == null) {
            throw new RuntimeException("还款计划不存在");
        }
        
        if ("paid".equals(schedule.getStatus())) {
            throw new RuntimeException("该期已还款");
        }
        
        // 更新计划状态
        schedule.setStatus("paid");
        schedule.setPaidDate(paymentDate);
        schedule.setPaidAmount(amount);
        scheduleMapper.update(schedule);
        
        // 创建还款记录
        PaymentRecord record = new PaymentRecord();
        record.setLoanId(schedule.getLoanId());
        record.setScheduleId(scheduleId);
        record.setAmount(amount);
        record.setPaymentDate(paymentDate);
        record.setPaymentType("normal");
        recordMapper.insert(record);
    }
    
    /**
     * 标记当天应还款项为已还
     * 返回标记的数量
     */
    @Transactional
    public int markTodayAsPaid() {
        LocalDate today = LocalDate.now();
        int count = 0;
        
        // 直接查询今天应还且状态为pending的计划
        List<RepaymentSchedule> todaySchedules = scheduleMapper.findTodayPending(today);
        
        for (RepaymentSchedule schedule : todaySchedules) {
            schedule.setStatus("paid");
            schedule.setPaidDate(today);
            schedule.setPaidAmount(schedule.getAmount());
            scheduleMapper.update(schedule);
            
            // 创建还款记录
            PaymentRecord record = new PaymentRecord();
            record.setLoanId(schedule.getLoanId());
            record.setScheduleId(schedule.getId());
            record.setAmount(schedule.getAmount());
            record.setPaymentDate(today);
            record.setPaymentType("auto");
            recordMapper.insert(record);
            
            count++;
        }
        
        return count;
    }
}
