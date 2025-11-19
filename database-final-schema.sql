-- =============================================
-- 贷款管理系统 - 最终数据库结构
-- 创建时间: 2025-11-19
-- 描述: 4表设计 + 视图 + 存储过程 + 触发器
-- =============================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS loans 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE loans;

-- =============================================
-- 1. 贷款主表 (loan)
-- =============================================
CREATE TABLE loan
(
    id             BIGINT AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    name           VARCHAR(100)                          NOT NULL COMMENT '贷款名称',
    platform       VARCHAR(50)                           NULL COMMENT '贷款平台',
    principal      DECIMAL(12, 2)                        NOT NULL COMMENT '借款本金',
    monthly_amount DECIMAL(10, 2)                        NOT NULL COMMENT '月还款额',
    total_periods  INT                                   NOT NULL COMMENT '总期数',
    payment_day    INT                                   NOT NULL COMMENT '每月还款日(1-31)',
    start_date     DATE                                  NOT NULL COMMENT '首期还款日期',
    status         VARCHAR(20) DEFAULT 'active'          NOT NULL COMMENT '状态: active-还款中, completed-已结清',
    note           TEXT                                  NULL COMMENT '备注信息',
    created_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='贷款主表';

CREATE INDEX idx_created_at ON loan (created_at);
CREATE INDEX idx_platform ON loan (platform);
CREATE INDEX idx_status ON loan (status);

-- =============================================
-- 2. 还款计划表 (repayment_schedule)
-- =============================================
CREATE TABLE repayment_schedule
(
    id          BIGINT AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    loan_id     BIGINT                                NOT NULL COMMENT '贷款ID',
    period      INT                                   NOT NULL COMMENT '期数(第几期)',
    due_date    DATE                                  NOT NULL COMMENT '应还日期',
    amount      DECIMAL(10, 2)                        NOT NULL COMMENT '应还金额',
    status      VARCHAR(20) DEFAULT 'pending'         NOT NULL COMMENT '状态: pending-待还, paid-已还, overdue-逾期',
    paid_date   DATE                                  NULL COMMENT '实际还款日期',
    paid_amount DECIMAL(10, 2)                        NULL COMMENT '实际还款金额',
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_loan_period UNIQUE (loan_id, period) COMMENT '同一贷款的期数唯一',
    CONSTRAINT repayment_schedule_ibfk_1
        FOREIGN KEY (loan_id) REFERENCES loan (id)
            ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='还款计划表-单一数据源';

CREATE INDEX idx_due_date ON repayment_schedule (due_date);
CREATE INDEX idx_loan_status ON repayment_schedule (loan_id, status);
CREATE INDEX idx_status ON repayment_schedule (status);

-- =============================================
-- 3. 还款记录表 (payment_record)
-- =============================================
CREATE TABLE payment_record
(
    id           BIGINT AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    loan_id      BIGINT                                NOT NULL COMMENT '贷款ID',
    schedule_id  BIGINT                                NULL COMMENT '关联的计划ID(正常还款时关联)',
    amount       DECIMAL(10, 2)                        NOT NULL COMMENT '还款金额',
    payment_date DATE                                  NOT NULL COMMENT '还款日期',
    payment_type VARCHAR(20) DEFAULT 'normal'          NULL COMMENT '还款类型: normal-正常还款, early-提前还款, extra-额外还款',
    note         TEXT                                  NULL COMMENT '备注',
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    CONSTRAINT payment_record_ibfk_1
        FOREIGN KEY (loan_id) REFERENCES loan (id)
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT payment_record_ibfk_2
        FOREIGN KEY (schedule_id) REFERENCES repayment_schedule (id)
            ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='还款记录表-历史追踪';

CREATE INDEX idx_loan_date ON payment_record (loan_id, payment_date);
CREATE INDEX idx_payment_date ON payment_record (payment_date);
CREATE INDEX idx_schedule_id ON payment_record (schedule_id);

-- =============================================
-- 4. 负债快照表 (debt_snapshot)
-- =============================================
CREATE TABLE debt_snapshot
(
    id               BIGINT AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    snapshot_date    DATE                                     NOT NULL COMMENT '快照日期',
    total_debt       DECIMAL(12, 2)                           NOT NULL COMMENT '总负债(所有未还金额)',
    total_principal  DECIMAL(12, 2) DEFAULT 0.00              NULL COMMENT '总本金',
    paid_amount      DECIMAL(12, 2) DEFAULT 0.00              NULL COMMENT '已还总额',
    remaining_amount DECIMAL(12, 2)                           NOT NULL COMMENT '剩余总额',
    active_loans     INT            DEFAULT 0                 NULL COMMENT '活跃贷款数量',
    completed_loans  INT            DEFAULT 0                 NULL COMMENT '已结清贷款数量',
    monthly_payment  DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '月还款总额',
    snapshot_type    VARCHAR(10)    DEFAULT 'daily'           NOT NULL COMMENT '快照类型: daily-每日, monthly-每月',
    created_at       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    CONSTRAINT uk_date_type UNIQUE (snapshot_date, snapshot_type) COMMENT '同一天同类型只有一条记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='负债快照表-用于趋势分析';

CREATE INDEX idx_snapshot_date ON debt_snapshot (snapshot_date);
CREATE INDEX idx_snapshot_type ON debt_snapshot (snapshot_type);

-- =============================================
-- 视图：贷款统计视图
-- =============================================
CREATE OR REPLACE VIEW v_loan_statistics AS
SELECT 
    l.id                                                                   AS id,
    l.name                                                                 AS name,
    l.platform                                                             AS platform,
    l.principal                                                            AS principal,
    l.monthly_amount                                                       AS monthly_amount,
    l.total_periods                                                        AS total_periods,
    l.payment_day                                                          AS payment_day,
    l.start_date                                                           AS start_date,
    l.status                                                               AS status,
    COUNT(s.id)                                                            AS total_schedule_count,
    SUM(CASE WHEN s.status = 'paid' THEN 1 ELSE 0 END)                    AS paid_periods,
    SUM(CASE WHEN s.status = 'pending' THEN 1 ELSE 0 END)                 AS pending_periods,
    SUM(CASE WHEN s.status = 'overdue' THEN 1 ELSE 0 END)                 AS overdue_periods,
    SUM(CASE WHEN s.status = 'paid' THEN s.paid_amount ELSE 0 END)        AS total_paid_amount,
    SUM(CASE WHEN s.status <> 'paid' THEN s.amount ELSE 0 END)            AS remaining_amount,
    MAX(s.due_date)                                                        AS last_due_date,
    MIN(CASE WHEN s.status <> 'paid' THEN s.due_date END)                 AS next_due_date
FROM loan l
LEFT JOIN repayment_schedule s ON l.id = s.loan_id
GROUP BY l.id;

-- =============================================
-- 触发器：自动更新逾期状态
-- =============================================
DELIMITER $$

CREATE TRIGGER tr_update_overdue_status
BEFORE UPDATE ON repayment_schedule
FOR EACH ROW
BEGIN
    IF NEW.status = 'pending' AND NEW.due_date < CURDATE() THEN
        SET NEW.status = 'overdue';
    END IF;
END$$

DELIMITER ;

-- =============================================
-- 存储过程：生成还款计划
-- =============================================
DELIMITER $$

CREATE PROCEDURE sp_generate_repayment_schedule(IN p_loan_id BIGINT)
BEGIN
    DECLARE v_total_periods INT;
    DECLARE v_monthly_amount DECIMAL(10,2);
    DECLARE v_payment_day INT;
    DECLARE v_start_date DATE;
    DECLARE v_period INT DEFAULT 1;
    DECLARE v_due_date DATE;

    -- 获取贷款信息
    SELECT total_periods, monthly_amount, payment_day, start_date
    INTO v_total_periods, v_monthly_amount, v_payment_day, v_start_date
    FROM loan
    WHERE id = p_loan_id;

    -- 删除已存在的计划
    DELETE FROM repayment_schedule WHERE loan_id = p_loan_id;

    -- 生成每期计划
    WHILE v_period <= v_total_periods DO
        -- 计算应还日期
        SET v_due_date = DATE_ADD(v_start_date, INTERVAL (v_period - 1) MONTH);

        -- 调整为指定还款日
        SET v_due_date = DATE_FORMAT(v_due_date, CONCAT('%Y-%m-', LPAD(v_payment_day, 2, '0')));

        -- 插入计划
        INSERT INTO repayment_schedule (loan_id, period, due_date, amount, status)
        VALUES (p_loan_id, v_period, v_due_date, v_monthly_amount, 'pending');

        SET v_period = v_period + 1;
    END WHILE;
END$$

DELIMITER ;

-- =============================================
-- 存储过程：创建每日快照
-- =============================================
DELIMITER $$

CREATE PROCEDURE sp_create_daily_snapshot()
BEGIN
    DECLARE v_snapshot_date DATE;
    DECLARE v_total_debt DECIMAL(12,2);
    DECLARE v_total_principal DECIMAL(12,2);
    DECLARE v_paid_amount DECIMAL(12,2);
    DECLARE v_remaining_amount DECIMAL(12,2);
    DECLARE v_active_loans INT;
    DECLARE v_completed_loans INT;
    DECLARE v_monthly_payment DECIMAL(10,2);

    SET v_snapshot_date = CURDATE();

    -- 计算统计数据
    SELECT
        COALESCE(SUM(CASE WHEN s.status != 'paid' THEN s.amount ELSE 0 END), 0),
        COALESCE(SUM(l.principal), 0),
        COALESCE(SUM(CASE WHEN s.status = 'paid' THEN s.paid_amount ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN s.status != 'paid' THEN s.amount ELSE 0 END), 0),
        COUNT(DISTINCT CASE WHEN l.status = 'active' THEN l.id END),
        COUNT(DISTINCT CASE WHEN l.status = 'completed' THEN l.id END),
        COALESCE(SUM(CASE WHEN l.status = 'active' THEN l.monthly_amount ELSE 0 END), 0)
    INTO
        v_total_debt,
        v_total_principal,
        v_paid_amount,
        v_remaining_amount,
        v_active_loans,
        v_completed_loans,
        v_monthly_payment
    FROM (`loans`.`loan` `l` left join `loans`.`repayment_schedule` `s` on ((`l`.`id` = `s`.`loan_id`)));

    -- 插入快照（如果今天已有则更新）
    INSERT INTO debt_snapshot (
        snapshot_date, total_debt, total_principal, paid_amount,
        remaining_amount, active_loans, completed_loans, monthly_payment, snapshot_type
    ) VALUES (
        v_snapshot_date, v_total_debt, v_total_principal, v_paid_amount,
        v_remaining_amount, v_active_loans, v_completed_loans, v_monthly_payment, 'daily'
    ) ON DUPLICATE KEY UPDATE
        total_debt = v_total_debt,
        total_principal = v_total_principal,
        paid_amount = v_paid_amount,
        remaining_amount = v_remaining_amount,
        active_loans = v_active_loans,
        completed_loans = v_completed_loans,
        monthly_payment = v_monthly_payment;
END$$

DELIMITER ;

-- =============================================
-- 完成提示
-- =============================================
SELECT '✅ 数据库结构创建完成！' AS message;
SELECT '📊 已创建 4 张表: loan, repayment_schedule, payment_record, debt_snapshot' AS info;
SELECT '👁️ 已创建视图: v_loan_statistics' AS info;
SELECT '🔧 已创建存储过程: sp_generate_repayment_schedule, sp_create_daily_snapshot' AS info;
SELECT '⚡ 已创建触发器: tr_update_overdue_status' AS info;

SHOW TABLES;
