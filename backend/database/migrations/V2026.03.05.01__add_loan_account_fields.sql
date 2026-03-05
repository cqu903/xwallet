-- Add new fields to loan_account table for post-loan management
ALTER TABLE `loan_account`
ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '账户状态: NORMAL/OVERDUE/FROZEN/CLOSED' AFTER `interest_outstanding`,
ADD COLUMN `penalty_rate` DECIMAL(10,6) DEFAULT 0.0005 COMMENT '罚息率（日利率）' AFTER `status`,
ADD COLUMN `earliest_overdue_date` DATE COMMENT '最早逾期日期' AFTER `penalty_rate`;

-- Add index for status field
ALTER TABLE `loan_account`
ADD INDEX `idx_loan_account_status` (`status`);
