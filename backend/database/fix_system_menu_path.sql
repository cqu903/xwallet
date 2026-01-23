-- 修复系统管理菜单的 path 问题
-- 问题描述: "系统管理"作为父级菜单容器，其 path 应该为 NULL，否则点击时会导航到 /system
-- 导致被通配符路由捕获，显示占位页而不是展开子菜单

USE xwallet;

-- 将"系统管理"菜单的 path 设置为 NULL
UPDATE sys_menu
SET path = NULL
WHERE permission = 'system:view';

-- 验证修改
SELECT id, menu_name, menu_type, path, permission, parent_id
FROM sys_menu
WHERE permission IN ('system:view', 'system:menu', 'system:role')
ORDER BY parent_id, sort_order;
