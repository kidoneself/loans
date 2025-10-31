-- =============================================
-- 个人资金 & 网贷管理系统 - 完整数据库初始化脚本
-- Version: 2.0.0
-- Author: lizhiqiang
-- Date: 2025-10-30
-- =============================================

-- =============================================
-- 1. 创建数据库
-- =============================================

-- 删除旧数据库（谨慎使用！会删除所有数据）
-- DROP DATABASE IF EXISTS loans;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS loans 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE loans;

-- =============================================
-- 2. 核心业务表
-- =============================================

-- 2.1 贷款表 (loans)
CREATE TABLE IF NOT EXISTS loans (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    loan_name VARCHAR(100) NOT NULL COMMENT '贷款名称',
    platform VARCHAR(50) COMMENT '贷款平台',
    total_amount DECIMAL(10,2) COMMENT '总借款金额',
    remaining_amount DECIMAL(10,2) NOT NULL COMMENT '剩余未还金额',
    monthly_payment DECIMAL(10,2) NOT NULL COMMENT '月还款额',
    payment_day INT NOT NULL COMMENT '每月还款日（1-31）',
    total_periods INT COMMENT '总还款期数',
    paid_periods INT DEFAULT 0 COMMENT '已还期数',
    start_date DATE COMMENT '贷款开始日期',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-活跃, completed-已完成, cancelled-已取消',
    note TEXT COMMENT '备注说明',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_status (status),
    INDEX idx_platform (platform),
    INDEX idx_payment_day (payment_day)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='贷款管理表';

-- 2.2 收入表 (income)
CREATE TABLE IF NOT EXISTS income (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    income_type VARCHAR(50) NOT NULL COMMENT '收入类型（工资、奖金等）',
    amount DECIMAL(10,2) NOT NULL COMMENT '收入金额',
    income_day INT NOT NULL COMMENT '每月收入日（1-31）',
    description TEXT COMMENT '收入说明',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用：1-是, 0-否',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收入管理表';

-- 2.3 固定支出表 (expenses_fixed)
CREATE TABLE IF NOT EXISTS expenses_fixed (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    expense_name VARCHAR(100) NOT NULL COMMENT '支出名称',
    amount DECIMAL(10,2) NOT NULL COMMENT '支出金额',
    expense_day INT NOT NULL COMMENT '每月支出日（1-31）',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用：1-是, 0-否',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='固定支出管理表';

-- 2.4 余额历史表 (balance_history)
CREATE TABLE IF NOT EXISTS balance_history (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    balance DECIMAL(10,2) NOT NULL COMMENT '当前余额',
    change_amount DECIMAL(10,2) DEFAULT 0 COMMENT '变动金额',
    change_type VARCHAR(20) NOT NULL COMMENT '变动类型：manual-手动, income-收入, expense-支出, payment-还款',
    related_id BIGINT COMMENT '关联业务ID',
    description TEXT COMMENT '变动说明',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_change_type (change_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='余额变动历史表';

-- 2.5 还款历史表 (payment_history)
CREATE TABLE IF NOT EXISTS payment_history (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    loan_id BIGINT NOT NULL COMMENT '贷款ID',
    payment_amount DECIMAL(10,2) NOT NULL COMMENT '还款金额',
    payment_date DATE NOT NULL COMMENT '还款日期',
    is_extra_payment TINYINT(1) DEFAULT 0 COMMENT '是否额外还款：1-是, 0-否',
    auto_deduct_balance TINYINT(1) DEFAULT 1 COMMENT '是否自动扣减余额：1-是, 0-否',
    note TEXT COMMENT '还款备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_loan_id (loan_id),
    INDEX idx_payment_date (payment_date),
    CONSTRAINT fk_payment_loan FOREIGN KEY (loan_id) 
        REFERENCES loans(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='还款历史记录表';

-- =============================================
-- 3. 工资周期预测功能表
-- =============================================

-- 3.1 系统配置表 (system_config)
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键（唯一）',
    config_value VARCHAR(200) COMMENT '配置值',
    description TEXT COMMENT '配置说明',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 3.2 临时收支表 (temp_transactions)
CREATE TABLE IF NOT EXISTS temp_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    transaction_date DATE NOT NULL COMMENT '交易日期',
    type VARCHAR(20) NOT NULL COMMENT '类型：income-收入, expense-支出',
    amount DECIMAL(10,2) NOT NULL COMMENT '交易金额',
    description TEXT COMMENT '交易说明',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='临时收支记录表';

-- =============================================
-- 4. 债务趋势分析表
-- =============================================

-- 4.1 债务快照表 (debt_snapshot)
CREATE TABLE IF NOT EXISTS debt_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    snapshot_date DATE NOT NULL UNIQUE COMMENT '快照日期（唯一）',
    total_debt DECIMAL(10,2) NOT NULL COMMENT '总欠款额',
    total_monthly_payment DECIMAL(10,2) NOT NULL COMMENT '月还款总额',
    active_loan_count INT NOT NULL COMMENT '活跃贷款数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_snapshot_date (snapshot_date),
    INDEX idx_snapshot_date_desc (snapshot_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='债务快照表（每日自动记录）';

-- =============================================
-- 5. 初始化数据
-- =============================================

-- 5.1 初始化余额记录
INSERT INTO balance_history (balance, change_amount, change_type, description) 
VALUES (0.00, 0.00, 'manual', '系统初始余额')
ON DUPLICATE KEY UPDATE id = id;

-- 5.2 初始化系统配置
INSERT INTO system_config (config_key, config_value, description) 
VALUES 
    ('salary_day', '20', '工资发放日（每月几号，1-31）'),
    ('salary_amount', '20000', '月工资金额（用于预测计算）'),
    ('current_balance', '0', '当前账户余额')
ON DUPLICATE KEY UPDATE config_key = config_key;

-- 5.3 初始化债务快照（当前数据）
INSERT INTO debt_snapshot (snapshot_date, total_debt, total_monthly_payment, active_loan_count)
SELECT 
    CURDATE() as snapshot_date,
    COALESCE(SUM(remaining_amount), 0) as total_debt,
    COALESCE(SUM(monthly_payment), 0) as total_monthly_payment,
    COUNT(*) as active_loan_count
FROM loans
WHERE status = 'active' AND remaining_amount > 0
ON DUPLICATE KEY UPDATE
    total_debt = VALUES(total_debt),
    total_monthly_payment = VALUES(total_monthly_payment),
    active_loan_count = VALUES(active_loan_count);

-- =============================================
-- 6. 验证表结构
-- =============================================

-- 查看所有表
SHOW TABLES;

-- 查看表记录数
SELECT 'loans' as table_name, COUNT(*) as count FROM loans
UNION ALL
SELECT 'income', COUNT(*) FROM income
UNION ALL
SELECT 'expenses_fixed', COUNT(*) FROM expenses_fixed
UNION ALL
SELECT 'balance_history', COUNT(*) FROM balance_history
UNION ALL
SELECT 'payment_history', COUNT(*) FROM payment_history
UNION ALL
SELECT 'system_config', COUNT(*) FROM system_config
UNION ALL
SELECT 'temp_transactions', COUNT(*) FROM temp_transactions
UNION ALL
SELECT 'debt_snapshot', COUNT(*) FROM debt_snapshot;

-- 查看系统配置
SELECT * FROM system_config ORDER BY id;

-- =============================================
-- 7. 使用说明
-- =============================================

/*
====================================
数据库初始化完成！
====================================

【表结构说明】

核心业务表：
1. loans            - 贷款管理表
2. income           - 收入管理表
3. expenses_fixed   - 固定支出表
4. balance_history  - 余额历史表
5. payment_history  - 还款历史表

功能扩展表：
6. system_config      - 系统配置表
7. temp_transactions  - 临时收支表
8. debt_snapshot      - 债务快照表

【下一步操作】

1. 配置系统参数：
   UPDATE system_config SET config_value = '你的工资发放日' WHERE config_key = 'salary_day';
   UPDATE system_config SET config_value = '你的工资金额' WHERE config_key = 'salary_amount';
   UPDATE system_config SET config_value = '你的当前余额' WHERE config_key = 'current_balance';

2. 添加收入信息：
   INSERT INTO income (income_type, amount, income_day, description) 
   VALUES ('工资', 20000, 20, '月工资收入');

3. 添加固定支出：
   INSERT INTO expenses_fixed (expense_name, amount, expense_day) 
   VALUES ('房租', 2000, 5);

4. 添加贷款信息：
   使用前端页面的"智能录入"功能添加贷款

【环境配置】

本地环境（prod）：
- 数据库：127.0.0.1:3306/loans
- 用户名：loans
- 密码：a123456

测试环境（test）：
- 数据库：10.10.10.17:3306/loans
- 用户名：root
- 密码：MyStrongPass123

【应用启动】
mvn spring-boot:run

访问地址：http://localhost:8080

====================================
*/

