package com.finance.loans.mapper;

import com.finance.loans.model.Income;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 收入 Mapper 接口
 */
@Mapper
public interface IncomeMapper {

    @Select("SELECT * FROM income WHERE id = #{id}")
    Income findById(Long id);

    @Select("SELECT * FROM income")
    List<Income> findAll();

    @Select("SELECT * FROM income WHERE is_active = #{isActive}")
    List<Income> findByIsActive(Boolean isActive);

    @Insert("INSERT INTO income (income_type, amount, income_day, description, is_active, created_at, updated_at) " +
            "VALUES (#{incomeType}, #{amount}, #{incomeDay}, #{description}, #{isActive}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Income income);

    @Update("UPDATE income SET income_type=#{incomeType}, amount=#{amount}, income_day=#{incomeDay}, " +
            "description=#{description}, is_active=#{isActive}, updated_at=NOW() WHERE id=#{id}")
    int update(Income income);

    @Delete("DELETE FROM income WHERE id = #{id}")
    int deleteById(Long id);
}

