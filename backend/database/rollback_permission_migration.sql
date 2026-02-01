-- ============================================
-- 权限系统回滚脚本
-- 日期: 2026-01-31
-- 说明: 回滚到旧的权限架构（菜单内嵌权限）
-- 警告: 此脚本将删除 sys_permission 和 sys_role_permission 表
-- ============================================

USE xwallet;

-- ============================================
-- 警告: 以下操作不可逆！
-- ============================================

-- 步骤1: 删除角色权限关联表
DROP TABLE IF EXISTS `sys_role_permission`;

-- 步骤2: 删除权限表
DROP TABLE IF EXISTS `sys_permission`;

-- 步骤3: 删除菜单表的 permission_id 字段
ALTER TABLE `sys_menu`
DROP COLUMN IF EXISTS `permission_id`;

-- ============================================
-- 回滚完成
-- ============================================

SELECT '============================================' AS '';
SELECT '回滚完成！' AS message;
SELECT '注意事项:' AS '';
SELECT '1. sys_role_permission 表已删除' AS '';
SELECT '2. sys_permission 表已删除' AS '';
SELECT '3. sys_menu.permission_id 字段已删除' AS '';
SELECT '4. sys_menu.permission 字段已恢复使用' AS '';
SELECT '============================================' AS '';
