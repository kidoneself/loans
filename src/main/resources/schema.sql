-- =============================================
-- 个人资金 & 网贷管理系统 - 数据库表结构
-- Database: MySQL
-- Version: 1.0.0
-- =============================================

-- 1. 贷款表 (loans)
CREATE TABLE IF NOT EXISTS loans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_name VARCHAR(100) NOT NULL,
    platform VARCHAR(50),
    total_amount DECIMAL(10,2),
    remaining_amount DECIMAL(10,2) NOT NULL,
    monthly_payment DECIMAL(10,2) NOT NULL,
    payment_day INT NOT NULL CHECK(payment_day BETWEEN 1 AND 31),
    total_periods INT,
    paid_periods INT DEFAULT 0,
    start_date DATE,
    status VARCHAR(20) DEFAULT 'active',
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 收入表 (income)
CREATE TABLE IF NOT EXISTS income (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    income_type VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    income_day INT NOT NULL CHECK(income_day BETWEEN 1 AND 31),
    description TEXT,
    is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 固定支出表 (expenses_fixed)
CREATE TABLE IF NOT EXISTS expenses_fixed (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    expense_name VARCHAR(100) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    expense_day INT NOT NULL CHECK(expense_day BETWEEN 1 AND 31),
    is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 余额历史表 (balance_history)
CREATE TABLE IF NOT EXISTS balance_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    balance DECIMAL(10,2) NOT NULL,
    change_amount DECIMAL(10,2) DEFAULT 0,
    change_type VARCHAR(20) NOT NULL,
    related_id BIGINT,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 还款历史表 (payment_history)
CREATE TABLE IF NOT EXISTS payment_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    payment_amount DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    is_extra_payment TINYINT(1) DEFAULT 0,
    auto_deduct_balance TINYINT(1) DEFAULT 1,
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 索引创建
-- =============================================

-- 贷款表索引
CREATE INDEX IF NOT EXISTS idx_loans_status ON loans(status);
CREATE INDEX IF NOT EXISTS idx_loans_platform ON loans(platform);
CREATE INDEX IF NOT EXISTS idx_loans_payment_day ON loans(payment_day);

-- 收入表索引
CREATE INDEX IF NOT EXISTS idx_income_active ON income(is_active);

-- 支出表索引
CREATE INDEX IF NOT EXISTS idx_expenses_active ON expenses_fixed(is_active);

-- 余额历史表索引
CREATE INDEX IF NOT EXISTS idx_balance_history_type ON balance_history(change_type);
CREATE INDEX IF NOT EXISTS idx_balance_history_date ON balance_history(created_at);

-- 还款历史表索引
CREATE INDEX IF NOT EXISTS idx_payment_history_loan ON payment_history(loan_id);
CREATE INDEX IF NOT EXISTS idx_payment_history_date ON payment_history(payment_date);

-- =============================================
-- 初始数据（可选）
-- =============================================

-- 插入默认余额记录
INSERT IGNORE INTO balance_history (balance, change_amount, change_type, description) 
VALUES (0.00, 0.00, 'manual', '初始余额');

