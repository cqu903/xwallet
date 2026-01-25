-- 为 sys_user 表添加软删除支持
-- 执行时间: 2025-01-25
-- 说明: 添加 deleted 字段用于软删除，0-未删除 1-已删除

-- 1. 添加软删除字段
ALTER TABLE `sys_user`
ADD COLUMN `deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除: 0-未删除 1-已删除'
AFTER `status`;

-- 2. 添加索引优化查询
CREATE INDEX `idx_deleted` ON `sys_user`(`deleted`);

-- 3. 验证字段已添加
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'sys_user'
  AND COLUMN_NAME = 'deleted';
