# Mapper 包统一重构设计

## 1. 背景

当前项目中 MyBatis Mapper 接口分散在两个包中：
- `com.zerofinance.xwallet.mapper`：2 个类（CollectionTaskMapper, CollectionRecordMapper）
- `com.zerofinance.xwallet.repository`：18 个类

这种不一致的包结构违反了单一职责原则，增加了代码维护成本。需要统一到一个包下，保持代码规范一致。

## 2. 目标

1. 合并所有开发分支到 main
2. 删除已合并的 feature 分支
3. 将所有 Mapper 接口统一到 `com.zerofinance.xwallet.mapper` 包
4. 删除 `repository` 包

## 3. 前置任务：分支合并

当前分支状态：
- `main`（当前分支）
- `feature/post-loan-management`
- `feature/repayment-with-contracts`
- `develop`（远程分支）

执行步骤：
1. 合并 `feature/post-loan-management` 到 `main`
2. 合并 `feature/repayment-with-contracts` 到 `main`
3. 删除本地和远程的 feature 分支

## 4. 影响范围

### 4.1 需要迁移的类（18 个）

从 `repository` 迁移到 `mapper`：
- CustomerMapper
- SysUserMapper
- SysRoleMapper
- SysMenuMapper
- SysPermissionMapper
- SysRoleMenuMapper
- SysRolePermissionMapper
- SysUserRoleMapper
- TokenBlacklistMapper
- VerificationCodeMapper
- LoanApplicationMapper
- LoanApplicationOtpMapper
- LoanAccountMapper
- LoanTransactionMapper
- LoanContractMapper
- LoanContractDocumentMapper
- RepaymentScheduleMapper
- AnalyticsEventMapper

### 4.2 需要更新的文件

1. **Java 源文件**
   - 所有 `import com.zerofinance.xwallet.repository.*Mapper` 语句
   - 预计影响 50-100 个文件

2. **MyBatis XML 文件**
   - `backend/src/main/resources/mapper/*.xml`
   - 更新 namespace 属性：`com.zerofinance.xwallet.repository.*Mapper` → `com.zerofinance.xwallet.mapper.*Mapper`

3. **启动类配置**
   - `XWalletBackendApplication.java`
   - `@MapperScan({"com.zerofinance.xwallet.repository", "com.zerofinance.xwallet.mapper"})`
   - 简化为：`@MapperScan("com.zerofinance.xwallet.mapper")`

## 5. 执行步骤

### 阶段 1：分支合并
1. 检查 feature 分支状态
2. 合并 feature/post-loan-management 到 main
3. 合并 feature/repayment-with-contracts 到 main
4. 删除本地 feature 分支
5. 删除远程 feature 分支

### 阶段 2：迁移 Java 文件
1. 移动 18 个 Mapper 接口文件从 `repository/` 到 `mapper/`
2. 更新每个文件的 `package` 声明（`repository` → `mapper`）

### 阶段 3：更新引用
1. 查找并替换所有 Java 文件中的 import 语句
2. 查找并更新 MyBatis XML 文件中的 namespace
3. 更新启动类 `XWalletBackendApplication.java` 的 `@MapperScan` 注解

### 阶段 4：清理与验证
1. 删除空的 `repository` 目录
2. 运行编译验证：`mvn clean compile`
3. 运行测试验证：`mvn test`
4. 确认无运行时错误

### 阶段 5：提交
1. 提交重构变更

## 6. 验证方式

### 6.1 编译验证
```bash
cd backend
mvn clean compile
```

### 6.2 测试验证
```bash
cd backend
mvn test
```

### 6.3 运行时验证
- 启动应用：`mvn spring-boot:run`
- 验证数据库操作正常
- 验证接口响应正常

## 7. 回滚方案

如果重构后发现问题，可以通过 git 回滚：
```bash
git reset --hard <重构前的commit-id>
```

## 8. 风险评估

- **风险等级**：低
- **影响范围**：仅包名和 import 语句，不涉及业务逻辑
- **测试覆盖**：现有测试用例可验证正确性
- **回滚难度**：低（git 一键回滚）
