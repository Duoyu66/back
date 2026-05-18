-- 系统设置菜单（系统管理下）
USE admin;

INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, icon, sort_order, status) VALUES
(91, 2, 'sys:settings', '系统设置', 1, '/settings', 'settings', 7, 1)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name), path = VALUES(path), icon = VALUES(icon);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, 91 FROM sys_role r WHERE r.id IN (1, 2);
