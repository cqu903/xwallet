# 用户管理功能设计文档

**日期**: 2025-01-21
**作者**: Claude Code
**状态**: 待实施

## 1. 功能概述

为管理后台（front/）添加完整的系统用户管理功能，支持：
- 创建系统用户（工号、姓名、密码、邮箱）
- 禁用用户（软删除，不提供物理删除）
- 为用户分配多个角色
- 用户列表搜索与筛选
- 编辑用户基本信息
- 重置用户密码

## 2. 数据库调整

### 2.1 修改 sys_user 表

```sql
-- 添加 email 字段
ALTER TABLE `sys_user` ADD COLUMN `email` VARCHAR(100) UNIQUE COMMENT '邮箱' AFTER `username`;

-- 删除 role 字段（完全使用 sys_user_role 管理多角色）
ALTER TABLE `sys_user` DROP COLUMN `role`;
```

### 2.2 依赖现有表

- `sys_user` - 用户基本信息
- `sys_role` - 角色定义
- `sys_user_role` - 用户角色关联（多对多）
- `sys_operation_log` - 操作日志审计

## 3. 后端 API 设计

### 3.1 接口列表

| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/api/user/list` | 分页查询用户列表 | `user:view` |
| GET | `/api/user/{id}` | 获取用户详情（含角色） | `user:view` |
| POST | `/api/user` | 创建用户 | `user:create` |
| PUT | `/api/user/{id}` | 编辑用户 | `user:update` |
| PUT | `/api/user/{id}/status` | 启用/禁用用户 | `user:update` |
| PUT | `/api/user/{id}/password` | 重置密码 | `user:resetPwd` |

### 3.2 DTO 设计

**UserQueryRequest**
```java
- String keyword        // 工号或姓名模糊搜索
- List<Long> roleIds    // 角色筛选
- Integer status        // 状态筛选：1-启用 0-禁用 null-全部
- Integer page          // 页码
- Integer size          // 每页数量
```

**UserResponse**
```java
- Long id
- String employeeNo
- String username
- String email
- Integer status
- List<Role> roles      // 角色列表
- LocalDateTime createdAt
- LocalDateTime updatedAt
```

**CreateUserRequest**
```java
- String employeeNo
- String username
- String email
- String password
- List<Long> roleIds    // 至少一个
```

**UpdateUserRequest**
```java
- String username
- String email
- List<Long> roleIds    // 至少一个
```

### 3.3 核心业务逻辑

**UserService**
- 创建用户时密码 BCrypt 加密
- 分配角色时先删除原有关联再插入新的
- 禁用用户后，AuthInterceptor 拦截登录

## 4. 前端 UI 设计

### 4.1 页面结构

```
┌─────────────────────────────────────────────────────┐
│  用户管理                                           │
├─────────────────────────────────────────────────────┤
│  [搜索框] [角色筛选▼] [状态筛选▼]    [新增用户]     │
├─────────────────────────────────────────────────────┤
│  工号    │ 姓名 │ 邮箱    │ 角色   │ 状态 │ 操作    │
│  ADMIN001 │ 张三 │ ...    │ [Badge]│ 正常 │ 编辑 ... │
│  OP002   │ 李四 │ ...    │ [Badge]│ 禁用 │ 编辑 ... │
├─────────────────────────────────────────────────────┤
│  共 10 条          [< 1 2 3 >]                     │
└─────────────────────────────────────────────────────┘
```

### 4.2 弹窗设计

**用户编辑弹窗（创建/编辑复用）**
- 创建模式：工号、姓名、邮箱、密码、角色（多选）
- 编辑模式：姓名、邮箱、角色（多选）

**重置密码弹窗**
- 新密码输入框

### 4.3 状态显示

- 启用：绿色标签 "正常"
- 禁用：红色标签 "已禁用"
- 角色：Badge 组件显示多个角色

## 5. 状态管理

### 5.1 UserProvider

```dart
class UserProvider extends ChangeNotifier {
  List<User> _users = [];
  int _total = 0;
  int _currentPage = 1;
  UserQuery _query = UserQuery();

  Future<void> loadUsers();
  Future<void> search(String keyword);
  Future<void> filterByRole(List<int> roleIds);
  Future<void> filterByStatus(int? status);
  Future<void> createUser(CreateUserRequest request);
  Future<void> updateUser(int id, UpdateUserRequest request);
  Future<void> toggleStatus(int id);
  Future<void> resetPassword(int id, String newPassword);
}
```

### 5.2 RoleProvider

```dart
class RoleProvider extends ChangeNotifier {
  List<Role> _roles = [];
  Future<void> loadRoles();  // 获取所有启用的角色
}
```

## 6. 错误处理与边界情况

### 6.1 后端验证

| 场景 | 处理方式 |
|------|----------|
| 工号重复 | 400 "工号已存在" |
| 邮箱重复 | 400 "邮箱已被使用" |
| 用户不存在 | 404 "用户不存在" |
| 禁用自己 | 400 "不能禁用当前登录用户" |
| 移除所有角色 | 400 "至少保留一个角色" |
| 禁用用户登录 | AuthInterceptor 返回 401 |

### 6.2 前端处理

- 表单验证：必填项、格式检查
- API 错误：SnackBar 提示
- 网络超时：统一拦截处理

## 7. 操作日志

记录以下操作到 `sys_operation_log`：
- 创建用户
- 编辑用户
- 启用/禁用用户
- 重置密码

## 8. 文件清单

### 后端新增/修改

**新增：**
- `controller/UserController.java`
- `service/UserService.java`
- `service/impl/UserServiceImpl.java`
- `repository/SysUserRepository.java` (扩展 Mapper)
- `model/dto/UserQueryRequest.java`
- `model/dto/UserResponse.java`
- `model/dto/CreateUserRequest.java`
- `model/dto/UpdateUserRequest.java`
- `model/dto/ResetPasswordRequest.java`
- `model/entity/SysUser.java` (添加 email 字段，删除 role 字段)

**修改：**
- `config/AuthInterceptor.java` (检查用户状态)
- `repository/SysUserMapper.xml` (添加查询 SQL)

### 前端新增/修改

**新增：**
- `lib/screens/user_management_screen.dart`
- `lib/widgets/user_dialog.dart`
- `lib/widgets/reset_password_dialog.dart`
- `lib/providers/user_provider.dart`
- `lib/providers/role_provider.dart`
- `lib/services/user_service.dart`
- `lib/models/user.dart`
- `lib/models/role.dart`

**修改：**
- `lib/routes/app_router.dart` (添加用户管理路由)
- `lib/providers/auth_provider.dart` (扩展角色信息)

### 数据库

**新增脚本：**
- `backend/database/user_management_update.sql`

## 9. 实施顺序

1. 数据库调整
2. 后端 Entity 和 DTO
3. 后端 Repository 和 Service
4. 后端 Controller
5. 前端 Models 和 Services
6. 前端 Providers
7. 前端 UI 组件和页面
8. 路由配置和权限配置
9. 测试
