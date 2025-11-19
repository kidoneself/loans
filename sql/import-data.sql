-- =============================================
-- 数据导入脚本 - 转换为 v3.0 表结构
-- 总共 21 笔贷款
-- =============================================

-- 设置客户端字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE loans;

-- 导入贷款数据（旧字段 → 新字段）
INSERT INTO loan (name, platform, principal, monthly_amount, payment_day, total_periods, start_date, status, note) VALUES
('信用飞20250510', '信用飞', 10000.00, 1004.57, 11, 12, '2025-05-10', 'active', '20250510   10000    剩余7期  每月11号   1004.57'),
('信用飞20250315', '信用飞', 10000.00, 1004.57, 15, 12, '2025-03-15', 'active', '20250315   10000    剩余5期  每月15号   1004.57'),
('招联金融20250830', '招联金融', 6000.00, 562.64, 19, 12, '2025-08-30', 'active', ''),
('分期乐20250802', '分期乐', 10000.00, 475.24, 25, 24, '2025-08-02', 'active', ''),
('分期乐20250808', '分期乐', 10000.00, 503.90, 25, 24, '2025-08-08', 'active', ''),
('分期乐20250913', '分期乐', 15000.00, 793.07, 25, 24, '2025-09-13', 'active', ''),
('拍拍贷20250210', '拍拍贷', 27230.00, 2574.83, 10, 12, '2025-02-10', 'active', ''),
('拍拍贷20251010', '拍拍贷', 3420.00, 340.16, 11, 12, '2025-10-10', 'active', ''),
('拍拍贷20241214', '拍拍贷', 8000.00, 756.47, 14, 12, '2024-12-14', 'active', ''),
('拍拍贷20250515_A', '拍拍贷', 1500.00, 141.83, 15, 12, '2025-05-15', 'active', ''),
('拍拍贷20250515_B', '拍拍贷', 14000.00, 1323.82, 15, 12, '2025-05-15', 'active', ''),
('拍拍贷20250715', '拍拍贷', 11270.00, 1065.68, 15, 12, '2025-07-15', 'active', ''),
('拍拍贷20250815', '拍拍贷', 20000.00, 1334.04, 15, 18, '2025-08-15', 'active', ''),
('拍拍贷20250825', '拍拍贷', 10090.00, 954.09, 25, 12, '2025-08-25', 'active', ''),
('汽车租金20240719', '象屿盈信', 99180.00, 3727.00, 19, 36, '2024-07-19', 'active', ''),
('中信分期20250530', '中信银行', 50000.00, 2463.41, 24, 24, '2025-05-30', 'active', ''),
('借呗20250705', '支付宝', 1000.00, 93.90, 26, 12, '2025-07-05', 'active', ''),
('借呗20250707', '支付宝', 3000.00, 281.34, 26, 12, '2025-07-07', 'active', ''),
('借呗20250711', '支付宝', 3000.00, 280.62, 26, 12, '2025-07-11', 'active', ''),
('借呗20250825', '支付宝', 900.00, 312.24, 26, 3, '2025-08-25', 'active', ''),
('借呗20251010', '支付宝', 2000.00, 187.11, 26, 12, '2025-10-10', 'active', '');

-- 验证导入
SELECT '✅ 已导入贷款数据' AS message;
SELECT COUNT(*) AS total_loans FROM loan;

-- 为所有贷款生成还款计划
DELIMITER $$

CREATE PROCEDURE temp_generate_all()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_loan_id BIGINT;
    DECLARE v_loan_name VARCHAR(100);
    DECLARE cur CURSOR FOR SELECT id, name FROM loan;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO v_loan_id, v_loan_name;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        CALL sp_generate_repayment_schedule(v_loan_id);
        SELECT CONCAT('✓ 已生成: ', v_loan_name) AS progress;
    END LOOP;
    
    CLOSE cur;
    SELECT '✅ 所有还款计划已生成' AS result;
END$$

DELIMITER ;

CALL temp_generate_all();
DROP PROCEDURE IF EXISTS temp_generate_all;

-- 自动标记历史还款（应还日期 < 今天）
UPDATE repayment_schedule
SET 
    status = 'paid',
    paid_date = due_date,
    paid_amount = amount
WHERE due_date < CURDATE()
  AND status = 'pending';

SELECT CONCAT('✓ 已标记 ', ROW_COUNT(), ' 期历史还款为已还') AS progress;

-- 创建初始快照
CALL sp_create_daily_snapshot();

-- 显示统计摘要
SELECT '========================================' AS '';
SELECT '📊 导入完成统计' AS '';
SELECT '========================================' AS '';

SELECT 
    '贷款总数' AS metric,
    COUNT(*) AS value
FROM loan
UNION ALL
SELECT 
    '总本金',
    SUM(principal)
FROM loan
UNION ALL
SELECT 
    '月还款总额',
    SUM(monthly_amount)
FROM loan
UNION ALL
SELECT 
    '还款计划总数',
    COUNT(*)
FROM repayment_schedule
UNION ALL
SELECT 
    '总负债',
    SUM(amount)
FROM repayment_schedule
WHERE status != 'paid';

-- 按平台统计
SELECT '========================================' AS '';
SELECT '📊 平台分布' AS '';
SELECT '========================================' AS '';

SELECT 
    platform AS 平台,
    COUNT(*) AS 贷款数,
    SUM(principal) AS 总本金,
    SUM(monthly_amount) AS 月还款
FROM loan
GROUP BY platform
ORDER BY SUM(monthly_amount) DESC;

-- 显示贷款列表
SELECT '========================================' AS '';
SELECT '📋 贷款列表' AS '';
SELECT '========================================' AS '';

SELECT 
    id,
    name AS 贷款名称,
    platform AS 平台,
    principal AS 本金,
    monthly_amount AS 月还款,
    total_periods AS 总期数,
    payment_day AS 还款日
FROM loan
ORDER BY platform, payment_day;
