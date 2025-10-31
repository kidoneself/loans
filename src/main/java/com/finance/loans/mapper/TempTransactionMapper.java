package com.finance.loans.mapper;

import com.finance.loans.model.TempTransaction;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 临时收支 Mapper 接口
 */
@Mapper
public interface TempTransactionMapper {

    @Select("SELECT * FROM temp_transactions WHERE id = #{id}")
    TempTransaction findById(Long id);

    @Select("SELECT * FROM temp_transactions ORDER BY transaction_date DESC, created_at DESC")
    List<TempTransaction> findAll();

    @Select("SELECT * FROM temp_transactions WHERE transaction_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY transaction_date DESC")
    List<TempTransaction> findByDateRange(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);

    @Select("SELECT * FROM temp_transactions WHERE type = #{type} " +
            "AND transaction_date BETWEEN #{startDate} AND #{endDate}")
    List<TempTransaction> findByTypeAndDateRange(@Param("type") String type,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Insert("INSERT INTO temp_transactions (transaction_date, type, amount, description, created_at) " +
            "VALUES (#{transactionDate}, #{type}, #{amount}, #{description}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TempTransaction transaction);

    @Update("UPDATE temp_transactions SET transaction_date=#{transactionDate}, type=#{type}, " +
            "amount=#{amount}, description=#{description} WHERE id=#{id}")
    int update(TempTransaction transaction);

    @Delete("DELETE FROM temp_transactions WHERE id = #{id}")
    int deleteById(Long id);
}

