package com.finance.loans.mapper;

import com.finance.loans.model.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 还款记录 Mapper 接口
 */
@Mapper
public interface PaymentRecordMapper {
    
    /**
     * 根据贷款ID查询还款记录
     */
    List<PaymentRecord> findByLoanId(@Param("loanId") Long loanId);
    
    /**
     * 查询指定月份的还款记录
     */
    List<PaymentRecord> findByMonth(@Param("year") int year, @Param("month") int month);
    
    /**
     * 查询指定日期范围的还款记录
     */
    List<PaymentRecord> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 插入还款记录
     */
    int insert(PaymentRecord record);
    
    /**
     * 根据ID查询
     */
    PaymentRecord findById(@Param("id") Long id);
    
    /**
     * 删除记录
     */
    int deleteById(@Param("id") Long id);
}
