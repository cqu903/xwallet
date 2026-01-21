-- ============================================
-- 用户管理功能数据库更新脚本
-- 日期: 2025-01-21
-- ============================================

USE xwallet;

-- ============================================
-- 1. 添加 email 字段
-- ============================================
SET @email_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'email'
);

SET @email_sql := IF(
    @email_exists = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `email` VARCHAR(100) UNIQUE COMMENT ''邮箱'' AFTER `username`',
    'SELECT ''email column already exists'''
);

PREPARE stmt FROM @email_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 2. 删除 role 字段
-- ============================================
SET @role_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'role'
);

SET @role_sql := IF(
    @role_exists > 0,
    'ALTER TABLE `sys_user` DROP COLUMN `role`',
    'SELECT ''role column already removed'''
);

PREPARE stmt FROM @role_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 3. 更新用户管理菜单权限（添加启用/禁用权限）
-- ============================================
-- 添加启用/禁用用户权限
INSERT IGNORE INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`)
SELECT
    id,
    '启用/禁用用户',
    3,
    'user:toggleStatus',
    5
FROM sys_menu
WHERE permission = 'user:view';

-- ============================================
-- 4. 初始化完成提示
-- ============================================
SELECT '数据库更新完成！' AS message;
