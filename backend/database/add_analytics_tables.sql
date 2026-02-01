-- ======================================
-- MQTT 埋点事件存储表
-- 用于存储移动端上报的 MQTT 事件
-- ======================================

-- 1. 埋点事件表
CREATE TABLE IF NOT EXISTS `analytics_event` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `event_id` VARCHAR(64) NOT NULL COMMENT '事件唯一ID（UUID）',
    `device_id` VARCHAR(128) NOT NULL COMMENT '设备ID',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '用户ID（顾客ID）',
    `event_type` VARCHAR(64) NOT NULL COMMENT '事件类型（login, payment_success等）',
    `environment` VARCHAR(16) NOT NULL DEFAULT 'prod' COMMENT '环境：prod/dev/test',
    `topic` VARCHAR(128) NOT NULL COMMENT 'MQTT主题',
    `payload` JSON NOT NULL COMMENT '事件完整payload（JSON）',

    -- 上下文信息
    `app_version` VARCHAR(32) DEFAULT NULL COMMENT 'App版本',
    `os` VARCHAR(32) DEFAULT NULL COMMENT '操作系统（iOS/Android）',
    `os_version` VARCHAR(32) DEFAULT NULL COMMENT '系统版本',
    `device_model` VARCHAR(64) DEFAULT NULL COMMENT '设备型号',
    `network_type` VARCHAR(32) DEFAULT NULL COMMENT '网络类型（wifi/4g/5g）',

    -- 风控相关
    `session_id` VARCHAR(64) DEFAULT NULL COMMENT '会话ID',
    `is_critical` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为风控关键事件',

    -- 元数据
    `received_at` BIGINT NOT NULL COMMENT '接收时间戳（毫秒）',
    `event_timestamp` BIGINT NOT NULL COMMENT '事件发生时间戳（毫秒）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据库创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '数据库更新时间',

    -- 索引
    UNIQUE KEY `uk_event_id` (`event_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_event_type` (`event_type`),
    INDEX `idx_environment` (`environment`),
    INDEX `idx_received_at` (`received_at`),
    INDEX `idx_is_critical` (`is_critical`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MQTT埋点事件表';

-- 2. 创建菜单：MQTT事件管理
-- menu_type: 1-目录 2-菜单 3-按钮
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `permission`, `icon`, `sort_order`, `visible`, `status`) VALUES
(2, 'MQTT事件', 2, '/system/mqtt-events', 'system:mqtt:query', 'activity', 200, 1, 1);

-- 3. 创建角色菜单关联（给管理员角色访问权限）
-- 假设管理员角色ID为1，获取刚插入的菜单ID
SET @mqtt_menu_id = LAST_INSERT_ID();
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, @mqtt_menu_id
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = @mqtt_menu_id
);

-- 4. 添加操作日志菜单（可选）
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `permission`, `icon`, `sort_order`, `visible`, `status`) VALUES
(2, '操作日志', 2, '/system/operation-logs', 'system:log:query', 'file-text', 201, 1, 1);

SET @log_menu_id = LAST_INSERT_ID();
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, @log_menu_id
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = @log_menu_id
);
