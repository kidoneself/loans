-- =============================================
-- 生成 INSERT 语句（手动复制结果）
-- 使用方法：在数据库客户端执行，复制结果粘贴到新文件
-- =============================================

USE loans;

-- =============================================
-- 1. 导出 loan 表数据
-- =============================================
SELECT CONCAT(
    'INSERT INTO loan (id, name, platform, principal, monthly_amount, total_periods, payment_day, start_date, status, note, created_at, updated_at) VALUES (',
    id, ', ',
    QUOTE(name), ', ',
    IFNULL(QUOTE(platform), 'NULL'), ', ',
    principal, ', ',
    monthly_amount, ', ',
    total_periods, ', ',
    payment_day, ', ',
    QUOTE(start_date), ', ',
    QUOTE(status), ', ',
    IFNULL(QUOTE(note), 'NULL'), ', ',
    QUOTE(created_at), ', ',
    QUOTE(updated_at), ');'
) AS loan_inserts
FROM loan
ORDER BY id;

-- =============================================
-- 2. 导出 repayment_schedule 表数据
-- =============================================
SELECT CONCAT(
    'INSERT INTO repayment_schedule (id, loan_id, period, due_date, amount, status, paid_date, paid_amount, created_at, updated_at) VALUES (',
    id, ', ',
    loan_id, ', ',
    period, ', ',
    QUOTE(due_date), ', ',
    amount, ', ',
    QUOTE(status), ', ',
    IFNULL(QUOTE(paid_date), 'NULL'), ', ',
    IFNULL(paid_amount, 'NULL'), ', ',
    QUOTE(created_at), ', ',
    QUOTE(updated_at), ');'
) AS schedule_inserts
FROM repayment_schedule
ORDER BY loan_id, period;

-- =============================================
-- 3. 导出 payment_record 表数据
-- =============================================
SELECT CONCAT(
    'INSERT INTO payment_record (id, loan_id, schedule_id, amount, payment_date, payment_type, note, created_at) VALUES (',
    id, ', ',
    loan_id, ', ',
    IFNULL(schedule_id, 'NULL'), ', ',
    amount, ', ',
    QUOTE(payment_date), ', ',
    IFNULL(QUOTE(payment_type), 'NULL'), ', ',
    IFNULL(QUOTE(note), 'NULL'), ', ',
    QUOTE(created_at), ');'
) AS payment_inserts
FROM payment_record
ORDER BY id;

-- =============================================
-- 4. 导出 debt_snapshot 表数据
-- =============================================
SELECT CONCAT(
    'INSERT INTO debt_snapshot (id, snapshot_date, total_debt, total_principal, paid_amount, remaining_amount, active_loans, completed_loans, monthly_payment, snapshot_type, created_at) VALUES (',
    id, ', ',
    QUOTE(snapshot_date), ', ',
    total_debt, ', ',
    total_principal, ', ',
    paid_amount, ', ',
    remaining_amount, ', ',
    active_loans, ', ',
    completed_loans, ', ',
    monthly_payment, ', ',
    QUOTE(snapshot_type), ', ',
    QUOTE(created_at), ');'
) AS snapshot_inserts
FROM debt_snapshot
ORDER BY snapshot_date;
