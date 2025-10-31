package com.finance.loans.mapper;

import com.finance.loans.model.PaymentHistory;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 还款历史 Mapper 接口
 */
@Mapper
public interface PaymentHistoryMapper {

    @Select("SELECT * FROM payment_history WHERE id = #{id}")
    PaymentHistory findById(Long id);

    @Select("SELECT * FROM payment_history ORDER BY payment_date DESC")
    List<PaymentHistory> findAll();

    @Select("SELECT * FROM payment_history WHERE loan_id = #{loanId} ORDER BY payment_date DESC")
    List<PaymentHistory> findByLoanId(Long loanId);

    @Insert("INSERT INTO payment_history (loan_id, payment_amount, payment_date, is_extra_payment, " +
            "auto_deduct_balance, note, created_at) " +
            "VALUES (#{loanId}, #{paymentAmount}, #{paymentDate}, #{isExtraPayment}, " +
            "#{autoDeductBalance}, #{note}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PaymentHistory paymentHistory);

    @Delete("DELETE FROM payment_history WHERE id = #{id}")
    int deleteById(Long id);
}

