-- ========================================
-- 顾客管理功能 - 数据库迁移脚本
-- ========================================

-- 1. 添加 last_login_time 字段到 customer 表（可选，如果需要显示最后登录时间）
-- ALTER TABLE `customer`
-- ADD COLUMN `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间' AFTER `created_at`;

-- 2. 插入顾客管理菜单（在"系统管理"下）
-- 注意：需要确保 system:view 权限的菜单已存在
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_order`)
SELECT id, '顾客管理', 2, '/customers', 'customers/index', 'customer:view', 'Users', 5
FROM sys_menu WHERE permission = 'system:view' LIMIT 1;

-- 3. 插入按钮权限（启用/禁用顾客）
-- 需要获取刚插入的顾客管理菜单的 ID
SET @customer_menu_id = LAST_INSERT_ID();

INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`)
VALUES (@customer_menu_id, '启用/禁用顾客', 3, 'customer:toggleStatus', 1);

-- 4. 授予 ADMIN 角色所有顾客管理权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT
    (SELECT id FROM sys_role WHERE role_code = 'ADMIN'),
    id
FROM sys_menu WHERE permission LIKE 'customer%';

-- 5. 授予 OPERATOR 角色查看权限（可选）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT
    (SELECT id FROM sys_role WHERE role_code = 'OPERATOR'),
    id
FROM sys_menu WHERE permission = 'customer:view';

-- 验证插入结果
SELECT * FROM sys_menu WHERE permission LIKE 'customer%' ORDER BY sort_order;
