package com.finance.loans.mapper;

import com.finance.loans.model.Expense;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 支出 Mapper 接口
 */
@Mapper
public interface ExpenseMapper {

    @Select("SELECT * FROM expenses_fixed WHERE id = #{id}")
    Expense findById(Long id);

    @Select("SELECT * FROM expenses_fixed")
    List<Expense> findAll();

    @Select("SELECT * FROM expenses_fixed WHERE is_active = #{isActive}")
    List<Expense> findByIsActive(Boolean isActive);

    @Insert("INSERT INTO expenses_fixed (expense_name, amount, expense_day, is_active, created_at, updated_at) " +
            "VALUES (#{expenseName}, #{amount}, #{expenseDay}, #{isActive}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Expense expense);

    @Update("UPDATE expenses_fixed SET expense_name=#{expenseName}, amount=#{amount}, expense_day=#{expenseDay}, " +
            "is_active=#{isActive}, updated_at=NOW() WHERE id=#{id}")
    int update(Expense expense);

    @Delete("DELETE FROM expenses_fixed WHERE id = #{id}")
    int deleteById(Long id);
}

