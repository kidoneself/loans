package com.finance.loans.service;

import com.finance.loans.mapper.PaymentRecordMapper;
import com.finance.loans.model.PaymentRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 还款记录服务
 */
@Service
public class PaymentRecordService {
    
    @Autowired
    private PaymentRecordMapper recordMapper;
    
    /**
     * 获取贷款的还款记录
     */
    public List<PaymentRecord> getLoanRecords(Long loanId) {
        return recordMapper.findByLoanId(loanId);
    }
    
    /**
     * 获取本月还款记录
     */
    public List<PaymentRecord> getCurrentMonthRecords() {
        LocalDate now = LocalDate.now();
        return recordMapper.findByMonth(now.getYear(), now.getMonthValue());
    }
    
    /**
     * 获取指定月份的还款记录
     */
    public List<PaymentRecord> getMonthRecords(int year, int month) {
        return recordMapper.findByMonth(year, month);
    }
}
