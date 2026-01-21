-- xWallet 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS xwallet DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE xwallet;

-- ============================================
-- 表1: 系统用户表 (内部员工)
-- ============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `employee_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '工号',
    `username` VARCHAR(100) NOT NULL COMMENT '用户姓名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `role` VARCHAR(50) NOT NULL COMMENT '角色: ADMIN/OPERATOR',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_employee_no` (`employee_no`),
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
-- 初始化数据
-- ============================================

-- 插入默认系统管理员
-- 密码: admin123 (BCrypt加密后的值, rounds=10)
-- 注意: 生产环境中应修改为强密码
INSERT INTO `sys_user` (`employee_no`, `username`, `password`, `role`, `status`) VALUES
('ADMIN001', '系统管理员', '$2a$10$SctOQzc3b3HDFRayZs/03Or/EX.gfIHigidU37gCoL7o88Nx6PUry', 'ADMIN', 1);

-- 插入测试顾客账号
-- 密码: customer123 (BCrypt加密后的值, rounds=10)
INSERT INTO `customer` (`email`, `password`, `nickname`, `status`) VALUES
('customer@example.com', '$2a$10$MGDPbtZ7beXyBsw7lK0S0u5sdAjdSFxkw3BBui2vOVClOScFnBweS', '测试顾客', 1);

-- ============================================
-- 测试账号说明
-- ============================================
-- 系统用户登录:
--   工号: ADMIN001
--   密码: admin123
--
-- 顾客登录:
--   邮箱: customer@example.com
--   密码: customer123
-- ============================================
