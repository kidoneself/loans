package com.finance.loans.mapper;

import com.finance.loans.model.DebtSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 负债快照 Mapper 接口
 */
@Mapper
public interface DebtSnapshotMapper {
    
    /**
     * 查询所有快照（按日期倒序）
     */
    List<DebtSnapshot> findAll();
    
    /**
     * 查询指定日期范围的快照
     */
    List<DebtSnapshot> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 查询最新快照
     */
    DebtSnapshot findLatest();
    
    /**
     * 根据日期和类型查询
     */
    DebtSnapshot findByDateAndType(@Param("snapshotDate") LocalDate snapshotDate, @Param("snapshotType") String snapshotType);
    
    /**
     * 插入快照
     */
    int insert(DebtSnapshot snapshot);
    
    /**
     * 删除指定日期之前的快照
     */
    int deleteBeforeDate(@Param("date") LocalDate date);
}
