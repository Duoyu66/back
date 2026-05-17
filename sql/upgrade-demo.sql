-- 实例演示（Ant Design 组件展示）菜单
USE admin;

INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, icon, sort_order, status) VALUES
(80, 0,  'demo',              '实例演示', 0, NULL,           'monitor',      4,  1),
(81, 80, 'demo:form',         '表单',     1, '/demo/form',     'file-text',    1,  1),
(82, 80, 'demo:table',        '表格',     1, '/demo/table',    'list',         2,  1),
(83, 80, 'demo:modal',        '弹框',     1, '/demo/modal',    'layout-dashboard', 3, 1),
(88, 80, 'demo:message',      '消息提示', 1, '/demo/message',  'bell',         4,  1),
(84, 80, 'demo:operation',    '操作',     1, '/demo/operation','wrench',       5,  1),
(85, 80, 'demo:chart',        '报表',     1, '/demo/chart',    'bar-chart-3',  6,  1),
(86, 80, 'demo:icon',         '图标',     1, '/demo/icon',     'bookmark',     7,  1),
(87, 80, 'demo:nested',       '四层菜单', 1, '/demo/nested',   'folder-tree',  8,  1),
(89, 80, 'demo:exception',  '缺省页',   1, '/demo/exception','shield',       9,  1),
(90, 80, 'demo:badge',      '菜单徽标', 1, '/demo/badge',    'bell',         10, 1)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name), path = VALUES(path), icon = VALUES(icon);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE id BETWEEN 80 AND 90;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE id BETWEEN 80 AND 88;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission WHERE id BETWEEN 80 AND 88;

-- 已有库补「消息提示」菜单（若已执行过旧版 upgrade-demo.sql）
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, icon, sort_order, status) VALUES
(88, 80, 'demo:message', '消息提示', 1, '/demo/message', 'bell', 4, 1)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name), path = VALUES(path), icon = VALUES(icon), sort_order = VALUES(sort_order);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, 88 FROM sys_role r WHERE r.id IN (1, 2, 3);
