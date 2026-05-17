-- 已有库升级：部门表、用户部门字段、菜单/部门权限
USE admin;

CREATE TABLE IF NOT EXISTS sys_dept (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '上级部门ID',
    dept_name   VARCHAR(64)  NOT NULL COMMENT '部门名称',
    dept_code   VARCHAR(64)  NOT NULL UNIQUE COMMENT '部门编码',
    leader      VARCHAR(64)  DEFAULT NULL COMMENT '负责人',
    phone       VARCHAR(32)  DEFAULT NULL COMMENT '联系电话',
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '部门';

-- 若列已存在请跳过本句
ALTER TABLE sys_user
    ADD COLUMN dept_id BIGINT DEFAULT NULL COMMENT '所属部门' AFTER avatar;

-- 部门管理菜单与按钮
INSERT INTO sys_permission (parent_id, perm_code, perm_name, perm_type, path, icon, sort_order) VALUES
(2, 'sys:dept', '部门管理', 1, '/depts', 'building-2', 4)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

SET @dept_menu_id = (SELECT id FROM sys_permission WHERE perm_code = 'sys:dept' LIMIT 1);

INSERT INTO sys_permission (parent_id, perm_code, perm_name, perm_type, sort_order) VALUES
(@dept_menu_id, 'sys:dept:list', '部门查询', 2, 1),
(@dept_menu_id, 'sys:dept:add', '部门新增', 2, 2),
(@dept_menu_id, 'sys:dept:edit', '部门编辑', 2, 3),
(@dept_menu_id, 'sys:dept:delete', '部门删除', 2, 4)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

SET @perm_menu_id = (SELECT id FROM sys_permission WHERE perm_code = 'sys:perm' LIMIT 1);

INSERT INTO sys_permission (parent_id, perm_code, perm_name, perm_type, sort_order) VALUES
(@perm_menu_id, 'sys:perm:add', '权限新增', 2, 2),
(@perm_menu_id, 'sys:perm:edit', '权限编辑', 2, 3),
(@perm_menu_id, 'sys:perm:delete', '权限删除', 2, 4)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

-- 初始部门
INSERT INTO sys_dept (id, parent_id, dept_name, dept_code, leader, sort_order) VALUES
(1, 0, '木瓜科技', 'ROOT', '管理员', 1),
(2, 1, '研发中心', 'RD', NULL, 1),
(3, 1, '运营中心', 'OPS', NULL, 2)
ON DUPLICATE KEY UPDATE dept_name = VALUES(dept_name);

-- ADMIN 角色补全新权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission
WHERE perm_code IN (
    'sys:dept', 'sys:dept:list', 'sys:dept:add', 'sys:dept:edit', 'sys:dept:delete',
    'sys:perm:add', 'sys:perm:edit', 'sys:perm:delete'
);

UPDATE sys_user SET dept_id = 1 WHERE id = 1 AND dept_id IS NULL;
