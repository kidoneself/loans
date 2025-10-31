package com.finance.loans.mapper;

import com.finance.loans.model.Loan;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 贷款 Mapper 接口
 */
@Mapper
public interface LoanMapper {

    @Select("SELECT * FROM loans WHERE id = #{id}")
    Loan findById(Long id);

    @Select("SELECT * FROM loans")
    List<Loan> findAll();

    @Select("SELECT * FROM loans WHERE status = #{status}")
    List<Loan> findByStatus(String status);

    @Insert("INSERT INTO loans (loan_name, platform, total_amount, remaining_amount, monthly_payment, " +
            "payment_day, total_periods, paid_periods, start_date, status, note, created_at, updated_at) " +
            "VALUES (#{loanName}, #{platform}, #{totalAmount}, #{remainingAmount}, #{monthlyPayment}, " +
            "#{paymentDay}, #{totalPeriods}, #{paidPeriods}, #{startDate}, #{status}, #{note}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Loan loan);

    @Update("UPDATE loans SET loan_name=#{loanName}, platform=#{platform}, total_amount=#{totalAmount}, " +
            "remaining_amount=#{remainingAmount}, monthly_payment=#{monthlyPayment}, payment_day=#{paymentDay}, " +
            "total_periods=#{totalPeriods}, paid_periods=#{paidPeriods}, start_date=#{startDate}, " +
            "status=#{status}, note=#{note}, updated_at=NOW() WHERE id=#{id}")
    int update(Loan loan);

    @Delete("DELETE FROM loans WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM loans WHERE status = 'active'")
    int countActive();
}

