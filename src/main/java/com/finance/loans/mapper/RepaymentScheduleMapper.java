package com.finance.loans.mapper;

import com.finance.loans.model.RepaymentSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 还款计划 Mapper 接口
 */
@Mapper
public interface RepaymentScheduleMapper {
    
    /**
     * 根据贷款ID查询还款计划
     */
    List<RepaymentSchedule> findByLoanId(@Param("loanId") Long loanId);
    
    /**
     * 根据贷款ID和状态查询还款计划
     */
    List<RepaymentSchedule> findByLoanIdAndStatus(@Param("loanId") Long loanId, @Param("status") String status);
    
    /**
     * 查询指定月份的还款计划 (在 XML 中定义)
     */
    List<RepaymentSchedule> findByMonth(@Param("year") int year, @Param("month") int month);
    
    /**
     * 查询今天应还且状态为pending的还款计划
     */
    @Select("SELECT * FROM repayment_schedule " +
            "WHERE due_date = #{today} AND status = 'pending' " +
            "ORDER BY id ASC")
    List<RepaymentSchedule> findTodayPending(@Param("today") LocalDate today);
    
    /**
     * 查询指定日期范围的还款计划
     */
    List<RepaymentSchedule> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 插入还款计划
     */
    int insert(RepaymentSchedule schedule);
    
    /**
     * 批量插入还款计划
     */
    int batchInsert(@Param("list") List<RepaymentSchedule> schedules);
    
    /**
     * 更新还款计划
     */
    int update(RepaymentSchedule schedule);
    
    /**
     * 批量更新状态
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") String status, 
                          @Param("paidDate") LocalDate paidDate);
    
    /**
     * 删除贷款的所有计划
     */
    int deleteByLoanId(@Param("loanId") Long loanId);
    
    /**
     * 根据ID查询
     */
    RepaymentSchedule findById(@Param("id") Long id);
}
