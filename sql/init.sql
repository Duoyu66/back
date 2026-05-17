-- =============================================================================
-- 木瓜后台 Admin — 完整数据库初始化脚本
-- 说明：会 DROP 并重建所有业务表，清空后重新导入初始数据
-- 默认账号：admin / editor / guest，密码均为 admin123
-- =============================================================================

CREATE DATABASE IF NOT EXISTS admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE admin;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS sys_notice_read;
DROP TABLE IF EXISTS sys_notice;
DROP TABLE IF EXISTS sys_oper_log;
DROP TABLE IF EXISTS sys_login_log;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_dept;

SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 用户表
-- -----------------------------------------------------------------------------
CREATE TABLE sys_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(64)  NOT NULL UNIQUE COMMENT '登录名',
    password    VARCHAR(128) NOT NULL COMMENT '密码(BCrypt)',
    nickname    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '昵称',
    email       VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    phone       VARCHAR(32)  DEFAULT NULL COMMENT '手机',
    avatar      VARCHAR(16)  DEFAULT NULL COMMENT '头像字母',
    dept_id     BIGINT       DEFAULT NULL COMMENT '所属部门',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统用户';

-- -----------------------------------------------------------------------------
-- 角色表
-- -----------------------------------------------------------------------------
CREATE TABLE sys_role (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code   VARCHAR(64)  NOT NULL UNIQUE COMMENT '角色编码',
    role_name   VARCHAR(64)  NOT NULL COMMENT '角色名称',
    description VARCHAR(255) DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '角色';

-- -----------------------------------------------------------------------------
-- 部门表
-- -----------------------------------------------------------------------------
CREATE TABLE sys_dept (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '上级部门ID，0为顶级',
    dept_name   VARCHAR(64)  NOT NULL COMMENT '部门名称',
    dept_code   VARCHAR(64)  NOT NULL UNIQUE COMMENT '部门编码',
    leader      VARCHAR(64)  DEFAULT NULL COMMENT '负责人',
    phone       VARCHAR(32)  DEFAULT NULL COMMENT '联系电话',
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '部门';

-- -----------------------------------------------------------------------------
-- 权限/菜单表  perm_type: 0目录 1菜单 2按钮
-- -----------------------------------------------------------------------------
CREATE TABLE sys_permission (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    perm_code   VARCHAR(128) NOT NULL UNIQUE COMMENT '权限标识',
    perm_name   VARCHAR(64)  NOT NULL,
    perm_type   TINYINT      NOT NULL DEFAULT 1 COMMENT '0目录 1菜单 2按钮',
    path        VARCHAR(128) DEFAULT NULL COMMENT '前端路由',
    icon        VARCHAR(64)  DEFAULT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT '权限菜单';

-- -----------------------------------------------------------------------------
-- 关联表
-- -----------------------------------------------------------------------------
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) COMMENT '用户角色';

CREATE TABLE sys_role_permission (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
) COMMENT '角色权限';

CREATE TABLE sys_notice (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    title        VARCHAR(128) NOT NULL,
    content      TEXT         NOT NULL,
    notice_type  TINYINT      NOT NULL DEFAULT 1 COMMENT '1通知 2公告',
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布',
    publish_time DATETIME     DEFAULT NULL,
    created_by   BIGINT       DEFAULT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统公告';

CREATE TABLE sys_notice_read (
    user_id   BIGINT   NOT NULL,
    notice_id BIGINT   NOT NULL,
    read_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, notice_id)
) COMMENT '公告已读';

CREATE TABLE sys_oper_log (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT       DEFAULT NULL,
    username       VARCHAR(64)  DEFAULT NULL,
    module         VARCHAR(64)  DEFAULT NULL,
    operation      VARCHAR(128) DEFAULT NULL,
    method         VARCHAR(256) DEFAULT NULL,
    request_uri    VARCHAR(256) DEFAULT NULL,
    request_method VARCHAR(16)  DEFAULT NULL,
    ip             VARCHAR(64)  DEFAULT NULL,
    user_agent     VARCHAR(512) DEFAULT NULL,
    params         TEXT         DEFAULT NULL,
    status         TINYINT      NOT NULL DEFAULT 1,
    error_msg      VARCHAR(512) DEFAULT NULL,
    cost_ms        BIGINT       DEFAULT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT '操作日志';

CREATE TABLE sys_login_log (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT       DEFAULT NULL,
    username   VARCHAR(64)  NOT NULL,
    ip         VARCHAR(64)  DEFAULT NULL,
    user_agent VARCHAR(512) DEFAULT NULL,
    status     TINYINT      NOT NULL DEFAULT 1,
    msg        VARCHAR(255) DEFAULT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT '登录日志';

-- =============================================================================
-- 初始数据
-- =============================================================================

-- 角色
INSERT INTO sys_role (id, role_code, role_name, description, status) VALUES
(1, 'ADMIN',  '超级管理员', '拥有全部权限', 1),
(2, 'EDITOR', '编辑',       '用户与内容相关权限', 1),
(3, 'GUEST',  '访客',       '只读权限', 1);

-- 部门
INSERT INTO sys_dept (id, parent_id, dept_name, dept_code, leader, sort_order, status) VALUES
(1, 0, '木瓜科技', 'ROOT', '管理员', 1, 1),
(2, 1, '研发中心', 'RD',   NULL,     1, 1),
(3, 1, '运营中心', 'OPS',  NULL,     2, 1);

-- 权限树
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, icon, sort_order, status) VALUES
-- 工作台
(1,  0,  'dashboard',       '工作台',   1, '/',            'layout-dashboard', 1,  1),
-- 系统管理（目录）
(2,  0,  'system',          '系统管理', 0, NULL,           'settings',         2,  1),
-- 用户管理
(10, 2,  'sys:user',        '用户管理', 1, '/users',       'users',            1,  1),
(11, 10, 'sys:user:list',   '用户查询', 2, NULL,           NULL,               1,  1),
(12, 10, 'sys:user:add',    '用户新增', 2, NULL,           NULL,               2,  1),
(13, 10, 'sys:user:edit',   '用户编辑', 2, NULL,           NULL,               3,  1),
(14, 10, 'sys:user:delete', '用户删除', 2, NULL,           NULL,               4,  1),
(15, 10, 'sys:user:reset',  '密码重置', 2, NULL,           NULL,               5,  1),
-- 角色管理
(20, 2,  'sys:role',        '角色管理', 1, '/roles',       'shield',           2,  1),
(21, 20, 'sys:role:list',   '角色查询', 2, NULL,           NULL,               1,  1),
(22, 20, 'sys:role:add',    '角色新增', 2, NULL,           NULL,               2,  1),
(23, 20, 'sys:role:edit',   '角色编辑', 2, NULL,           NULL,               3,  1),
(24, 20, 'sys:role:delete', '角色删除', 2, NULL,           NULL,               4,  1),
(25, 20, 'sys:role:perm',   '权限分配', 2, NULL,           NULL,               5,  1),
-- 权限配置
(30, 2,  'sys:perm',        '权限配置', 1, '/permissions', 'key-round',        3,  1),
(31, 30, 'sys:perm:list',   '权限查询', 2, NULL,           NULL,               1,  1),
(32, 30, 'sys:perm:add',    '权限新增', 2, NULL,           NULL,               2,  1),
(33, 30, 'sys:perm:edit',   '权限编辑', 2, NULL,           NULL,               3,  1),
(34, 30, 'sys:perm:delete', '权限删除', 2, NULL,           NULL,               4,  1),
-- 部门管理
(35, 2,  'sys:dept',        '部门管理', 1, '/depts',       'building-2',       4,  1),
(36, 35, 'sys:dept:list',   '部门查询', 2, NULL,           NULL,               1,  1),
(37, 35, 'sys:dept:add',    '部门新增', 2, NULL,           NULL,               2,  1),
(38, 35, 'sys:dept:edit',   '部门编辑', 2, NULL,           NULL,               3,  1),
(39, 35, 'sys:dept:delete', '部门删除', 2, NULL,           NULL,               4,  1),
-- 公告管理
(50, 2,  'sys:notice',      '公告管理', 1, '/notices',     'bell',             5,  1),
(51, 50, 'sys:notice:list',   '公告查询', 2, NULL,           NULL,               1,  1),
(52, 50, 'sys:notice:add',    '公告新增', 2, NULL,           NULL,               2,  1),
(53, 50, 'sys:notice:edit',   '公告编辑', 2, NULL,           NULL,               3,  1),
(54, 50, 'sys:notice:delete', '公告删除', 2, NULL,           NULL,               4,  1),
(55, 50, 'sys:notice:publish','公告发布', 2, NULL,           NULL,               5,  1),
-- 日志管理
(60, 2,  'sys:log',         '日志管理', 1, '/logs',        'scroll-text',      6,  1),
(61, 60, 'sys:log:oper',    '操作日志', 2, NULL,           NULL,               1,  1),
(62, 60, 'sys:log:login',   '登录日志', 2, NULL,           NULL,               2,  1),
-- 个人中心
(40, 0,  'profile',         '个人中心', 1, '/profile',     'user-circle',      99, 1);

INSERT INTO sys_notice (title, content, notice_type, status, publish_time, created_by) VALUES
('欢迎使用木瓜后台', '系统已上线，请及时修改默认密码并完善个人信息。', 2, 1, NOW(), 1);

-- 用户（密码 admin123）
INSERT INTO sys_user (id, username, password, nickname, email, avatar, dept_id, status) VALUES
(1, 'admin',  '$2a$10$9wL9ejuW46EPWPZ.Vosip.kD3zNzXWTsgvqFwNaKScvf4BdQ6xm9C', '超级管理员', 'admin@local.com',  'A', 1, 1),
(2, 'editor', '$2a$10$9wL9ejuW46EPWPZ.Vosip.kD3zNzXWTsgvqFwNaKScvf4BdQ6xm9C', '编辑员',     'editor@local.com', 'E', 2, 1),
(3, 'guest',  '$2a$10$9wL9ejuW46EPWPZ.Vosip.kD3zNzXWTsgvqFwNaKScvf4BdQ6xm9C', '访客',       'guest@local.com',  'G', 3, 1);

-- 用户角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3);

-- 角色权限：ADMIN 全部
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 角色权限：EDITOR（工作台、系统目录、用户管理除删除、个人中心）
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 10), (2, 11), (2, 12), (2, 13), (2, 15), (2, 40);

-- 角色权限：GUEST（工作台、用户查询、个人中心）
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(3, 1), (3, 10), (3, 11), (3, 40);

-- 自增起点（可选，避免与固定 ID 冲突）
ALTER TABLE sys_user AUTO_INCREMENT = 100;
ALTER TABLE sys_role AUTO_INCREMENT = 100;
ALTER TABLE sys_dept AUTO_INCREMENT = 100;
ALTER TABLE sys_permission AUTO_INCREMENT = 100;
