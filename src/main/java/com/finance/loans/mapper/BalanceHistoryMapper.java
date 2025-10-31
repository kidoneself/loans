package com.finance.loans.mapper;

import com.finance.loans.model.BalanceHistory;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 余额历史 Mapper 接口
 */
@Mapper
public interface BalanceHistoryMapper {

    @Select("SELECT * FROM balance_history WHERE id = #{id}")
    BalanceHistory findById(Long id);

    @Select("SELECT * FROM balance_history ORDER BY created_at DESC")
    List<BalanceHistory> findAll();

    @Select("SELECT * FROM balance_history ORDER BY created_at DESC LIMIT 1")
    BalanceHistory findLatest();

    @Select("SELECT * FROM balance_history WHERE change_type = #{changeType} ORDER BY created_at DESC")
    List<BalanceHistory> findByChangeType(String changeType);

    @Insert("INSERT INTO balance_history (balance, change_amount, change_type, related_id, description, created_at) " +
            "VALUES (#{balance}, #{changeAmount}, #{changeType}, #{relatedId}, #{description}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BalanceHistory balanceHistory);

    @Delete("DELETE FROM balance_history WHERE id = #{id}")
    int deleteById(Long id);
}

