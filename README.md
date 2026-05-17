# Admin 后台管理系统 — 后端

Spring Boot 2.7 + MyBatis-Plus + MySQL 8 + JWT + Spring Security

## 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0

## 启动步骤

1. 创建数据库并导入脚本：

```bash
mysql -u root -p < sql/schema.sql
```

2. 修改 `src/main/resources/application.yml` 中的数据库账号密码（默认 `root/root`）

3. 启动：

```bash
mvn spring-boot:run
```

服务地址：`http://localhost:8080`

## 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | ADMIN | 全部菜单与接口 |
| editor | admin123 | EDITOR | 用户管理（无删除）、工作台 |
| guest | admin123 | GUEST | 仅查询用户、工作台 |

## 主要接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录，返回 JWT |
| GET | /api/auth/info | 当前用户、菜单、权限 |
| PUT | /api/auth/profile | 修改个人信息 |
| PUT | /api/auth/password | 修改密码 |
| GET | /api/users | 用户分页 |
| POST/PUT/DELETE | /api/users | 用户增删改 |
| PUT | /api/users/{id}/reset-password | 重置密码 |
| GET | /api/roles | 角色分页 |
| GET | /api/roles/all | 全部角色（下拉） |
| PUT | /api/roles/{id}/permissions | 角色权限分配 |
| GET | /api/permissions/tree | 权限树 |

请求头：`Authorization: Bearer <token>`
