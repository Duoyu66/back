-- 将菜单图标更新为 Lucide kebab-case 名称
USE admin;

UPDATE sys_permission SET icon = 'layout-dashboard' WHERE perm_code = 'dashboard';
UPDATE sys_permission SET icon = 'settings' WHERE perm_code = 'system';
UPDATE sys_permission SET icon = 'users' WHERE perm_code = 'sys:user';
UPDATE sys_permission SET icon = 'shield' WHERE perm_code = 'sys:role';
UPDATE sys_permission SET icon = 'key-round' WHERE perm_code = 'sys:perm';
UPDATE sys_permission SET icon = 'user-circle' WHERE perm_code = 'profile';
