-- 实例演示：缺省页、菜单徽标
USE admin;

INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, icon, sort_order, status) VALUES
(89, 80, 'demo:exception', '缺省页',   1, '/demo/exception', 'shield',     9,  1),
(90, 80, 'demo:badge',     '菜单徽标', 1, '/demo/badge',     'bell',       10, 1)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name), path = VALUES(path), icon = VALUES(icon), sort_order = VALUES(sort_order);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r
CROSS JOIN (SELECT 89 AS id UNION SELECT 90) p
WHERE r.id IN (1, 2, 3);
