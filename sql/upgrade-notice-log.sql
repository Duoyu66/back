-- 公告、日志功能升级（已有库执行）
USE admin;

CREATE TABLE IF NOT EXISTS sys_notice (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    title        VARCHAR(128) NOT NULL,
    content      TEXT         NOT NULL,
    notice_type  TINYINT      NOT NULL DEFAULT 1,
    status       TINYINT      NOT NULL DEFAULT 0,
    publish_time DATETIME     DEFAULT NULL,
    created_by   BIGINT       DEFAULT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统公告';

CREATE TABLE IF NOT EXISTS sys_notice_read (
    user_id   BIGINT   NOT NULL,
    notice_id BIGINT   NOT NULL,
    read_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, notice_id)
) COMMENT '公告已读';

CREATE TABLE IF NOT EXISTS sys_oper_log (
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

CREATE TABLE IF NOT EXISTS sys_login_log (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT       DEFAULT NULL,
    username   VARCHAR(64)  NOT NULL,
    ip         VARCHAR(64)  DEFAULT NULL,
    user_agent VARCHAR(512) DEFAULT NULL,
    status     TINYINT      NOT NULL DEFAULT 1,
    msg        VARCHAR(255) DEFAULT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT '登录日志';

INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, icon, sort_order, status) VALUES
(50, 2,  'sys:notice',       '公告管理', 1, '/notices', 'bell', 5, 1),
(51, 50, 'sys:notice:list',    '公告查询', 2, NULL, NULL, 1, 1),
(52, 50, 'sys:notice:add',     '公告新增', 2, NULL, NULL, 2, 1),
(53, 50, 'sys:notice:edit',    '公告编辑', 2, NULL, NULL, 3, 1),
(54, 50, 'sys:notice:delete',  '公告删除', 2, NULL, NULL, 4, 1),
(55, 50, 'sys:notice:publish', '公告发布', 2, NULL, NULL, 5, 1),
(60, 2,  'sys:log',          '日志管理', 1, '/logs', 'scroll-text', 6, 1),
(61, 60, 'sys:log:oper',     '操作日志', 2, NULL, NULL, 1, 1),
(62, 60, 'sys:log:login',    '登录日志', 2, NULL, NULL, 2, 1)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE id IN (50,51,52,53,54,55,60,61,62);

INSERT INTO sys_notice (title, content, notice_type, status, publish_time, created_by)
SELECT '欢迎使用木瓜后台', '系统已上线，请及时修改默认密码并完善个人信息。', 2, 1, NOW(), 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_notice LIMIT 1);
