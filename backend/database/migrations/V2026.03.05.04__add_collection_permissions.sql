-- Add post-loan management menu
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, status)
VALUES 
(0, '贷后管理', 1, '/post-loan', NULL, 'post-loan:view', 'ClipboardList', 30, 1);

-- Get the parent menu ID
SET @post_loan_menu_id = LAST_INSERT_ID();

-- Add collection task menu
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, status)
VALUES 
(@post_loan_menu_id, '催收任务', 1, '/post-loan/collection-tasks', 'post-loan/collection-tasks/index', 'collection:task:view', 'Users', 1, 1);

-- Get collection task menu ID
SET @collection_menu_id = LAST_INSERT_ID();

-- Add button permissions for collection tasks
INSERT INTO sys_menu (parent_id, menu_name, menu_type, permission, sort_order, status)
VALUES 
(@collection_menu_id, '查看催收任务', 2, 'collection:task:view', 1, 1),
(@collection_menu_id, '分配催收任务', 2, 'collection:task:assign', 2, 1),
(@collection_menu_id, '更新催收状态', 2, 'collection:task:update', 3, 1),
(@collection_menu_id, '添加跟进记录', 2, 'collection:record:create', 4, 1),
(@collection_menu_id, '导出催收记录', 2, 'collection:record:export', 5, 1);

-- Assign permissions to ADMIN role
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT (SELECT id FROM sys_role WHERE role_code = 'ADMIN'), id
FROM sys_menu
WHERE permission LIKE 'collection:%';

-- Assign view permissions to OPERATOR role (if exists)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT (SELECT id FROM sys_role WHERE role_code = 'OPERATOR'), id
FROM sys_menu
WHERE permission IN ('collection:task:view', 'collection:record:create')
AND EXISTS (SELECT 1 FROM sys_role WHERE role_code = 'OPERATOR');

-- Create COLLECTOR role
INSERT INTO sys_role (role_code, role_name, description, sort_order, status, created_at, updated_at)
VALUES ('COLLECTOR', '催收员', '负责催收任务的跟进和管理', 4, 1, NOW(), NOW());

-- Assign permissions to COLLECTOR role
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT (SELECT id FROM sys_role WHERE role_code = 'COLLECTOR'), id
FROM sys_menu
WHERE permission IN (
    'post-loan:view',
    'collection:task:view',
    'collection:record:create'
);
