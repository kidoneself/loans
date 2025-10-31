package com.finance.loans.mapper;

import com.finance.loans.model.DebtSnapshot;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 债务快照 Mapper 接口
 */
@Mapper
public interface DebtSnapshotMapper {

    @Select("SELECT * FROM debt_snapshot WHERE id = #{id}")
    DebtSnapshot findById(Long id);

    @Select("SELECT * FROM debt_snapshot WHERE snapshot_date = #{date}")
    DebtSnapshot findBySnapshotDate(LocalDate date);

    @Select("SELECT * FROM debt_snapshot WHERE snapshot_date BETWEEN #{startDate} AND #{endDate} ORDER BY snapshot_date ASC")
    List<DebtSnapshot> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT * FROM debt_snapshot ORDER BY snapshot_date DESC LIMIT #{limit}")
    List<DebtSnapshot> findRecent(int limit);

    @Select("SELECT * FROM debt_snapshot ORDER BY snapshot_date DESC LIMIT 1")
    DebtSnapshot findLatest();

    @Insert("INSERT INTO debt_snapshot (snapshot_date, total_debt, total_monthly_payment, active_loan_count, created_at) " +
            "VALUES (#{snapshotDate}, #{totalDebt}, #{totalMonthlyPayment}, #{activeLoanCount}, NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "total_debt = #{totalDebt}, " +
            "total_monthly_payment = #{totalMonthlyPayment}, " +
            "active_loan_count = #{activeLoanCount}")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrUpdate(DebtSnapshot snapshot);

    @Update("UPDATE debt_snapshot SET total_debt=#{totalDebt}, total_monthly_payment=#{totalMonthlyPayment}, " +
            "active_loan_count=#{activeLoanCount} WHERE id=#{id}")
    int update(DebtSnapshot snapshot);

    @Delete("DELETE FROM debt_snapshot WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM debt_snapshot")
    int count();
}

