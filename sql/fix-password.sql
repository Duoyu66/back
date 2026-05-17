-- 将测试账号密码重置为 admin123（已导入旧 schema 时执行）
USE admin;

UPDATE sys_user SET password = '$2a$10$9wL9ejuW46EPWPZ.Vosip.kD3zNzXWTsgvqFwNaKScvf4BdQ6xm9C'
WHERE username IN ('admin', 'editor', 'guest');
