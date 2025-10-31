package com.finance.loans.mapper;

import com.finance.loans.model.SystemConfig;
import org.apache.ibatis.annotations.*;

/**
 * 系统配置 Mapper 接口
 */
@Mapper
public interface SystemConfigMapper {

    @Select("SELECT * FROM system_config WHERE config_key = #{key}")
    SystemConfig findByKey(String key);

    @Insert("INSERT INTO system_config (config_key, config_value, description, created_at, updated_at) " +
            "VALUES (#{configKey}, #{configValue}, #{description}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE config_value = #{configValue}, updated_at = NOW()")
    int upsert(SystemConfig config);

    @Update("UPDATE system_config SET config_value = #{value}, updated_at = NOW() WHERE config_key = #{key}")
    int updateByKey(@Param("key") String key, @Param("value") String value);
}

