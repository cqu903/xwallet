-- 将「用户管理」调整为「系统管理」的子菜单
-- 用于已按旧版 init_all.sql 初始化的数据库，无需重建
-- 执行: docker exec -i <mysql-container> mysql -u root -p xwallet < backend/database/migrate_user_menu_under_system.sql

USE xwallet;

-- 1. 将用户管理的 parent_id 设为系统管理，sort_order 置为 1（系统管理下第一项）
UPDATE sys_menu
SET parent_id = (SELECT id FROM (SELECT id FROM sys_menu WHERE permission = 'system:view') t),
    sort_order = 1
WHERE permission = 'user:view';

-- 2. 菜单管理、角色管理顺延：2、3
UPDATE sys_menu SET sort_order = 2 WHERE permission = 'system:menu';
UPDATE sys_menu SET sort_order = 3 WHERE permission = 'system:role';

-- 验证：系统管理及其子菜单
SELECT id, menu_name, menu_type, path, permission, parent_id, sort_order
FROM sys_menu
WHERE permission IN ('system:view', 'user:view', 'system:menu', 'system:role')
ORDER BY parent_id, sort_order;
