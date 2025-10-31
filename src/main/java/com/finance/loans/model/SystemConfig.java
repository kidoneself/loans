package com.finance.loans.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统配置实体类
 */
@Data
public class SystemConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

