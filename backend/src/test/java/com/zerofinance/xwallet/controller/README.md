# 菜单接口 E2E 测试说明

## 概述

`MenuE2ETest` 是一个端到端（E2E）测试类，用于测试菜单接口的完整流程，包括：
- 用户登录获取 token
- 使用 token 访问菜单接口
- 验证返回的菜单数据结构

## 前置条件

### 1. 数据库准备

确保 MySQL 数据库已启动并包含测试数据：

```bash
# 如果使用 Docker
docker exec -i <mysql-container-name> mysql -u root -p123321qQ < backend/database/init_all.sql

# 或直接连接 MySQL
mysql -u root -p
source backend/database/init_all.sql
```

### 2. 环境变量配置

测试需要以下环境变量（可以通过 `.env` 文件或系统环境变量设置）：

```bash
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/xwallet?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=123321qQ

# JWT 配置
JWT_SECRET=your-secret-key-here-must-be-at-least-256-bits-long

# 邮件配置（测试时会使用默认值，不会实际发送邮件）
MAIL_HOST=localhost
MAIL_PORT=25
MAIL_USERNAME=test@example.com
MAIL_PASSWORD=test
```

### 3. 测试账号

确保数据库中存在以下测试账号：

**系统用户：**
- 工号: `ADMIN001`
- 密码: `admin123`

**顾客用户：**
- 邮箱: `customer@example.com`
- 密码: `customer123`

## 运行测试

### 方式1: 使用 Maven 运行单个测试类

```bash
cd backend
mvn test -Dtest=MenuE2ETest
```

### 方式2: 运行所有测试

```bash
cd backend
mvn test
```

### 方式3: 在 IDE 中运行

在 IntelliJ IDEA 或 Eclipse 中：
1. 右键点击 `MenuE2ETest.java`
2. 选择 "Run 'MenuE2ETest'"

## 测试用例说明

### 1. testSystemUserLoginAndGetMenus
- **描述**: 测试系统用户登录并获取菜单列表
- **步骤**:
  1. 使用系统用户账号登录
  2. 获取 JWT token
  3. 使用 token 访问菜单接口
  4. 验证返回的菜单数据结构和内容
- **验证点**:
  - 登录成功，返回有效 token
  - 菜单接口返回成功
  - 菜单列表不为空
  - 系统管理菜单存在且包含子菜单

### 2. testUnauthorizedAccess
- **描述**: 测试未授权访问菜单接口
- **验证点**: 应该返回 401 未授权错误

### 3. testInvalidTokenAccess
- **描述**: 测试使用无效 token 访问菜单接口
- **验证点**: 应该返回 401 未授权错误

### 4. testCustomerLoginAndGetMenus
- **描述**: 测试顾客用户登录并尝试获取菜单
- **验证点**: 接口能正常响应（顾客用户可能返回空菜单列表）

## 测试输出示例

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## 常见问题

### Q1: 测试失败 - 数据库连接错误
**解决方案**:
1. 确认 MySQL 服务已启动
2. 检查 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 环境变量是否正确
3. 确认数据库 `xwallet` 已创建

### Q2: 测试失败 - JWT_SECRET 未设置
**解决方案**:
1. 设置 `JWT_SECRET` 环境变量
2. 或在 `.env` 文件中配置

### Q3: 测试失败 - 找不到测试账号
**解决方案**:
1. 确认已执行数据库初始化脚本 `init_all.sql`
2. 检查测试账号是否存在：
   ```sql
   SELECT * FROM sys_user WHERE employee_no = 'ADMIN001';
   SELECT * FROM customer WHERE email = 'customer@example.com';
   ```

### Q4: 测试超时
**解决方案**:
1. 检查数据库连接是否正常
2. 确认网络连接正常
3. 增加测试超时时间（在测试类中添加 `@Timeout` 注解）

## 注意事项

1. **E2E 测试需要完整环境**: 与单元测试不同，E2E 测试需要真实的数据库连接和 Spring Boot 应用上下文
2. **测试数据**: 测试会使用数据库中的真实数据，请确保测试数据的一致性
3. **测试隔离**: 建议在测试数据库中运行，避免影响开发数据
4. **性能**: E2E 测试运行时间较长，适合在 CI/CD 流程中运行

## 集成到 CI/CD

可以将此测试集成到持续集成流程中：

```yaml
# GitHub Actions 示例
- name: Run E2E Tests
  run: |
    cd backend
    mvn test -Dtest=MenuE2ETest
  env:
    DB_URL: ${{ secrets.DB_URL }}
    DB_USERNAME: ${{ secrets.DB_USERNAME }}
    DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
    JWT_SECRET: ${{ secrets.JWT_SECRET }}
```
