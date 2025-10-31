package com.finance.loans.service;

import com.finance.loans.mapper.SystemConfigMapper;
import com.finance.loans.model.SystemConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 系统配置服务
 */
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigMapper configMapper;

    /**
     * 获取配置值
     */
    public String getConfigValue(String key, String defaultValue) {
        SystemConfig config = configMapper.findByKey(key);
        return config != null ? config.getConfigValue() : defaultValue;
    }

    /**
     * 获取工资日
     */
    public int getSalaryDay() {
        String value = getConfigValue("salary_day", "20");
        return Integer.parseInt(value);
    }

    /**
     * 设置工资日
     */
    public void setSalaryDay(int day) {
        configMapper.updateByKey("salary_day", String.valueOf(day));
    }

    /**
     * 获取工资金额
     */
    public double getSalaryAmount() {
        String value = getConfigValue("salary_amount", "0");
        return Double.parseDouble(value);
    }

    /**
     * 设置工资金额
     */
    public void setSalaryAmount(double amount) {
        configMapper.updateByKey("salary_amount", String.valueOf(amount));
    }
}

