-- ============================================
-- 权限系统迁移脚本 - 独立权限表方案
-- 日期: 2026-01-31
-- 说明: 将菜单权限迁移到独立的权限表
-- ============================================

USE xwallet;

-- ============================================
-- 第一部分: 创建新表
-- ============================================

-- 创建权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    `permission_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限标识(如: user:create)',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称(如: 创建用户)',
    `resource_type` VARCHAR(20) NOT NULL COMMENT '资源类型: MENU-菜单 BUTTON-按钮 API-接口',
    `description` VARCHAR(500) COMMENT '权限描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_permission_code` (`permission_code`),
    INDEX `idx_resource_type` (`resource_type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================
-- 第二部分: 修改菜单表
-- ============================================

-- 添加权限外键字段（检查列是否已存在）
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'xwallet'
      AND TABLE_NAME = 'sys_menu'
      AND COLUMN_NAME = 'permission_id'
);

-- 只有列不存在时才添加
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `sys_menu` ADD COLUMN `permission_id` BIGINT COMMENT ''关联权限ID'' AFTER `component`',
    'SELECT ''Column permission_id already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加索引（检查索引是否已存在）
SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'xwallet'
      AND TABLE_NAME = 'sys_menu'
      AND INDEX_NAME = 'idx_permission_id'
);

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE `sys_menu` ADD INDEX `idx_permission_id` (`permission_id`)',
    'SELECT ''Index idx_permission_id already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 第三部分: 数据迁移
-- ============================================

-- 步骤1: 迁移菜单权限到权限表
INSERT INTO `sys_permission` (permission_code, permission_name, resource_type, description)
SELECT DISTINCT
    permission,
    CONCAT('权限:', permission) as permission_name,
    CASE
        WHEN menu_type = 1 THEN 'MENU'
        WHEN menu_type = 2 THEN 'MENU'
        WHEN menu_type = 3 THEN 'BUTTON'
    END as resource_type,
    CONCAT('从菜单迁移: ', menu_name) as description
FROM `sys_menu`
WHERE permission IS NOT NULL
  AND permission != ''
  AND status = 1
ON DUPLICATE KEY UPDATE
    permission_name = VALUES(permission_name),
    resource_type = VALUES(resource_type),
    description = VALUES(description);

-- 步骤2: 更新菜单表的 permission_id
UPDATE `sys_menu` m
INNER JOIN `sys_permission` p ON m.permission = p.permission_code
SET m.permission_id = p.id
WHERE m.permission IS NOT NULL
  AND m.permission != '';

-- 步骤3: 为 ADMIN 角色分配所有权限
INSERT IGNORE INTO `sys_role_permission` (role_id, permission_id)
SELECT r.id, p.id
FROM `sys_role` r
CROSS JOIN `sys_permission` p
WHERE r.role_code = 'ADMIN';

-- ============================================
-- 第四部分: 初始化额外权限数据
-- ============================================

-- 用户管理权限
INSERT INTO `sys_permission` (permission_code, permission_name, resource_type, description) VALUES
('user:view', '查看用户', 'MENU', '访问用户管理页面'),
('user:create', '创建用户', 'BUTTON', '创建新用户'),
('user:update', '编辑用户', 'BUTTON', '编辑用户信息'),
('user:delete', '删除用户', 'BUTTON', '删除用户'),
('user:resetPwd', '重置密码', 'BUTTON', '重置用户密码'),
('user:toggleStatus', '切换用户状态', 'BUTTON', '启用/禁用用户')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

-- 角色管理权限
INSERT INTO `sys_permission` (permission_code, permission_name, resource_type, description) VALUES
('role:view', '查看角色', 'MENU', '访问角色管理页面'),
('role:create', '创建角色', 'BUTTON', '创建新角色'),
('role:update', '编辑角色', 'BUTTON', '编辑角色信息'),
('role:delete', '删除角色', 'BUTTON', '删除角色')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

-- 菜单管理权限
INSERT INTO `sys_permission` (permission_code, permission_name, resource_type, description) VALUES
('menu:view', '查看菜单', 'MENU', '访问菜单管理页面'),
('menu:create', '创建菜单', 'BUTTON', '创建新菜单'),
('menu:update', '编辑菜单', 'BUTTON', '编辑菜单信息'),
('menu:delete', '删除菜单', 'BUTTON', '删除菜单')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

-- 系统管理权限
INSERT INTO `sys_permission` (permission_code, permission_name, resource_type, description) VALUES
('system:view', '查看系统管理', 'MENU', '访问系统管理模块'),
('system:permission', '权限管理', 'BUTTON', '管理权限配置')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

-- 权限管理权限
INSERT INTO `sys_permission` (permission_code, permission_name, resource_type, description) VALUES
('permission:view', '查看权限', 'MENU', '访问权限管理页面'),
('permission:create', '创建权限', 'BUTTON', '创建新权限'),
('permission:update', '编辑权限', 'BUTTON', '编辑权限信息'),
('permission:delete', '删除权限', 'BUTTON', '删除权限')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

-- 钱包管理权限
INSERT INTO `sys_permission` (permission_code, permission_name, resource_type, description) VALUES
('wallet:view', '查看钱包', 'MENU', '访问钱包管理页面'),
('wallet:create', '创建钱包', 'BUTTON', '创建新钱包'),
('wallet:freeze', '冻结钱包', 'BUTTON', '冻结钱包'),
('wallet:detail', '钱包详情', 'BUTTON', '查看钱包详情')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

-- 交易记录权限
INSERT INTO `sys_permission` (permission_code, permission_name, resource_type, description) VALUES
('transaction:view', '查看交易', 'MENU', '访问交易记录页面')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

-- 仪表盘权限
INSERT INTO `sys_permission` (permission_code, permission_name, resource_type, description) VALUES
('dashboard:view', '查看仪表盘', 'MENU', '访问仪表盘页面')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

-- ============================================
-- 第五部分: 验证数据一致性
-- ============================================

-- 验证1: 检查权限数量
SELECT 'Total permissions:' AS info, COUNT(*) AS count FROM `sys_permission`;

-- 验证2: 检查菜单权限关联
SELECT 'Menus with permissions:' AS info, COUNT(*) AS count FROM `sys_menu` WHERE `permission_id` IS NOT NULL;

-- 验证3: 检查 ADMIN 角色权限数量
SELECT 'ADMIN role permissions:' AS info, COUNT(*) AS count
FROM `sys_role_permission`
WHERE role_id = (SELECT id FROM `sys_role` WHERE role_code = 'ADMIN');

-- 验证4: 检查孤立菜单（有 permission 但没有 permission_id）
SELECT 'Orphaned menus (needs attention):' AS info, COUNT(*) AS count
FROM `sys_menu`
WHERE permission IS NOT NULL
  AND permission != ''
  AND permission_id IS NULL;

SELECT '============================================' AS '';
SELECT '数据库迁移完成！' AS message;
SELECT '注意事项:' AS '';
SELECT '1. sys_menu.permission 字段已废弃，请使用 permission_id' AS '';
SELECT '2. 所有权限现在通过 sys_permission 表管理' AS '';
SELECT '3. 角色权限通过 sys_role_permission 表关联' AS '';
SELECT '4. ADMIN 角色已自动分配所有权限' AS '';
SELECT '============================================' AS '';
