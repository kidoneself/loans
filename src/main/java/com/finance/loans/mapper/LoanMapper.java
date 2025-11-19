package com.finance.loans.mapper;

import com.finance.loans.model.Loan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 贷款 Mapper 接口
 */
@Mapper
public interface LoanMapper {
    
    /**
     * 查询所有贷款
     */
    List<Loan> findAll();
    
    /**
     * 根据状态查询贷款
     */
    List<Loan> findByStatus(@Param("status") String status);
    
    /**
     * 根据ID查询贷款
     */
    Loan findById(@Param("id") Long id);
    
    /**
     * 插入贷款
     */
    int insert(Loan loan);
    
    /**
     * 更新贷款
     */
    int update(Loan loan);
    
    /**
     * 删除贷款
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 统计贷款数量
     */
    int countByStatus(@Param("status") String status);
}
