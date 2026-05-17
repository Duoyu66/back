-- 系统监控模块（已有库执行）
USE admin;

INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, icon, sort_order, status) VALUES
(70, 0,  'sys:monitor',           '系统监控', 0, NULL,              'activity', 3,  1),
(71, 70, 'sys:monitor:online',    '在线用户', 1, '/monitor/online', 'users',    1,  1),
(72, 70, 'sys:monitor:job',       '定时任务', 1, '/monitor/job',    'clock',    2,  1),
(73, 70, 'sys:monitor:datasource','数据监控', 1, '/monitor/datasource', 'database', 3, 1),
(74, 70, 'sys:monitor:server',    '服务监控', 1, '/monitor/server', 'server',   4,  1),
(75, 70, 'sys:monitor:cache',     '缓存监控', 1, '/monitor/cache',  'package',  5,  1),
(76, 71, 'sys:monitor:kick',      '强退用户', 2, NULL,              NULL,       1,  1)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name), path = VALUES(path), icon = VALUES(icon);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE id BETWEEN 70 AND 76;
