-- ============================================
-- xWallet 完整数据库初始化脚本
-- 合并了 init.sql, rbac_init.sql, user_management_update.sql
-- 日期: 2025-01-22
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS xwallet DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE xwallet;

-- ============================================
-- 第一部分: 基础表创建
-- ============================================

-- ============================================
-- 表1: 系统用户表 (内部员工)
-- ============================================
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_operation_log`;
DROP TABLE IF EXISTS `sys_role_menu`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `employee_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '工号',
    `username` VARCHAR(100) NOT NULL COMMENT '用户姓名',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
    `remarks` VARCHAR(500) COMMENT '备注',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_employee_no` (`employee_no`),
    INDEX `idx_email` (`email`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ============================================
-- 表2: 顾客表 (外部客户)
-- ============================================
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '顾客ID',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(100) COMMENT '昵称',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-正常 0-冻结',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_email` (`email`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾客表';

-- ============================================
-- 表3: Token黑名单表
-- ============================================
DROP TABLE IF EXISTS `token_blacklist`;
CREATE TABLE `token_blacklist` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `token` VARCHAR(500) NOT NULL COMMENT '被加入黑名单的token',
    `expiry_time` DATETIME NOT NULL COMMENT 'token过期时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_token` (`token`(255)),
    INDEX `idx_expiry_time` (`expiry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token黑名单表';

-- ============================================
-- 表4: 验证码表
-- ============================================
DROP TABLE IF EXISTS `verification_code`;
CREATE TABLE `verification_code` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `email` VARCHAR(255) NOT NULL COMMENT '邮箱',
    `code` VARCHAR(6) NOT NULL COMMENT '验证码',
    `code_type` VARCHAR(20) NOT NULL COMMENT '验证码类型: REGISTER, RESET_PASSWORD',
    `expired_at` DATETIME NOT NULL COMMENT '过期时间',
    `verified` BOOLEAN DEFAULT FALSE COMMENT '是否已验证',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_email_type` (`email`, `code_type`),
    INDEX `idx_expired_at` (`expired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码表';

-- ============================================
-- 第二部分: RBAC 权限系统表
-- ============================================

-- ============================================
-- 表5: 菜单权限表 (核心表)
-- ============================================
CREATE TABLE `sys_menu` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID,0表示顶级菜单',
    `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `menu_type` TINYINT NOT NULL COMMENT '菜单类型: 1-目录 2-菜单 3-按钮',
    `path` VARCHAR(200) COMMENT '路由地址(前端路由path)',
    `component` VARCHAR(200) COMMENT '组件路径(前端组件路径)',
    `permission` VARCHAR(100) COMMENT '权限标识(如: user:create, user:delete)',
    `icon` VARCHAR(100) COMMENT '菜单图标',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `visible` TINYINT DEFAULT 1 COMMENT '显示状态: 1-显示 0-隐藏',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_permission` (`permission`),
    INDEX `idx_menu_type` (`menu_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- ============================================
-- 表6: 角色表
-- ============================================
CREATE TABLE `sys_role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    `role_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码(如: ADMIN, OPERATOR)',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(500) COMMENT '角色描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_role_code` (`role_code`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ============================================
-- 表7: 角色菜单关联表
-- ============================================
CREATE TABLE `sys_role_menu` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ============================================
-- 表8: 用户角色关联表
-- ============================================
CREATE TABLE `sys_user_role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================
-- 表9: 操作日志表(审计)
-- ============================================
CREATE TABLE `sys_operation_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT COMMENT '操作用户ID',
    `username` VARCHAR(100) COMMENT '操作用户名',
    `operation` VARCHAR(100) COMMENT '操作模块',
    `method` VARCHAR(200) COMMENT '请求方法',
    `params` TEXT COMMENT '请求参数',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `status` TINYINT COMMENT '状态: 1-成功 0-失败',
    `error_msg` VARCHAR(2000) COMMENT '错误信息',
    `execute_time` BIGINT COMMENT '执行时长(毫秒)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_operation` (`operation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 第三部分: 初始化数据
-- ============================================

-- ============================================
-- 1. 插入默认系统管理员
-- ============================================
-- 密码: admin123 (BCrypt加密后的值, rounds=10)
-- 注意: 生产环境中应修改为强密码
INSERT INTO `sys_user` (`employee_no`, `username`, `email`, `password`, `status`) VALUES
('ADMIN001', '系统管理员', 'admin@xwallet.com', '$2a$10$V8FBni1KNm2/4q3I1WVUGO6arThiUMWFOUKLxJ/KHu5QkudcPhgEy', 1);

-- ============================================
-- 2. 插入测试顾客账号
-- ============================================
-- 密码: customer123 (BCrypt加密后的值, rounds=10)
INSERT INTO `customer` (`email`, `password`, `nickname`, `status`) VALUES
('customer@example.com', '$2a$10$JwCb95fazP37G77GDTfdY.JPmdIa5C/HVFYcGaERl9vCMzgz95i5O', '测试顾客', 1);

-- ============================================
-- 3. 初始化菜单数据
-- ============================================

-- 一级菜单: 仪表盘
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_order`) VALUES
(0, '仪表盘', 2, '/dashboard', 'dashboard/index', 'dashboard:view', 'Dashboard', 1);

-- 一级菜单: 用户管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_order`) VALUES
(0, '用户管理', 2, '/users', 'users/index', 'user:view', 'User', 2);

-- 用户管理按钮权限
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`) VALUES
((SELECT id FROM (SELECT id FROM sys_menu WHERE permission = 'user:view') t), '新增用户', 3, 'user:create', 1),
((SELECT id FROM (SELECT id FROM sys_menu WHERE permission = 'user:view') t), '编辑用户', 3, 'user:update', 2),
((SELECT id FROM (SELECT id FROM sys_menu WHERE permission = 'user:view') t), '删除用户', 3, 'user:delete', 3),
((SELECT id FROM (SELECT id FROM sys_menu WHERE permission = 'user:view') t), '重置密码', 3, 'user:resetPwd', 4),
((SELECT id FROM (SELECT id FROM sys_menu WHERE permission = 'user:view') t), '启用/禁用用户', 3, 'user:toggleStatus', 5);

-- 一级菜单: 钱包管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_order`) VALUES
(0, '钱包管理', 2, '/wallets', 'wallets/index', 'wallet:view', 'Wallet', 3);

-- 钱包管理按钮权限
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`) VALUES
((SELECT id FROM (SELECT id FROM sys_menu WHERE permission = 'wallet:view') t), '创建钱包', 3, 'wallet:create', 1),
((SELECT id FROM (SELECT id FROM sys_menu WHERE permission = 'wallet:view') t), '冻结钱包', 3, 'wallet:freeze', 2),
((SELECT id FROM (SELECT id FROM sys_menu WHERE permission = 'wallet:view') t), '钱包详情', 3, 'wallet:detail', 3);

-- 一级菜单: 交易记录
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_order`) VALUES
(0, '交易记录', 2, '/transactions', 'transactions/index', 'transaction:view', 'Transaction', 4);

-- 一级菜单: 系统管理(仅管理员)
-- 注意: path 设置为 NULL，因为系统管理只是父级容器菜单，不需要导航
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_order`) VALUES
(0, '系统管理', 2, NULL, 'system/index', 'system:view', 'Setting', 99);

-- 系统管理子菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `sort_order`) VALUES
((SELECT id FROM (SELECT id FROM sys_menu WHERE permission = 'system:view') t), '菜单管理', 2, '/system/menus', 'system/menus/index', 'system:menu', 1),
((SELECT id FROM (SELECT id FROM sys_menu WHERE permission = 'system:view') t), '角色管理', 2, '/system/roles', 'system/roles/index', 'system:role', 2);

-- ============================================
-- 4. 初始化角色数据
-- ============================================
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `sort_order`) VALUES
('ADMIN', '超级管理员', '拥有所有权限', 1),
('OPERATOR', '操作员', '钱包日常操作权限', 2),
('VIEWER', '查看员', '只读权限', 3);

-- ============================================
-- 5. 初始化角色菜单关联 (ADMIN 拥有所有权限)
-- ============================================
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT
    (SELECT id FROM sys_role WHERE role_code = 'ADMIN'),
    id
FROM sys_menu;

-- ============================================
-- 6. 初始化角色菜单关联 (OPERATOR 权限)
-- ============================================
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT
    (SELECT id FROM sys_role WHERE role_code = 'OPERATOR'),
    id
FROM sys_menu
WHERE permission IN (
    'dashboard:view',
    'wallet:view',
    'wallet:detail',
    'transaction:view'
);

-- ============================================
-- 7. 初始化角色菜单关联 (VIEWER 权限)
-- ============================================
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT
    (SELECT id FROM sys_role WHERE role_code = 'VIEWER'),
    id
FROM sys_menu
WHERE permission IN (
    'dashboard:view',
    'transaction:view'
);

-- ============================================
-- 8. 初始化用户角色关联 (默认管理员为 ADMIN 角色)
-- ============================================
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT
    u.id,
    (SELECT id FROM sys_role WHERE role_code = 'ADMIN')
FROM sys_user u
WHERE u.employee_no = 'ADMIN001';

-- ============================================
-- 初始化完成
-- ============================================

-- 测试账号说明
-- 系统用户登录:
--   工号: ADMIN001
--   密码: admin123
--
-- 顾客登录:
--   邮箱: customer@example.com
--   密码: customer123
-- ============================================

SELECT '数据库初始化完成！' AS message;
