# Mapper 包统一重构实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 合并所有开发分支，然后将所有 MyBatis Mapper 接口统一到 `com.zerofinance.xwallet.mapper` 包

**Architecture:** 先合并分支，再通过移动文件、更新包声明、批量替换 import 和 XML namespace 来完成重构，最后通过编译和测试验证

**Tech Stack:** Java 21, Spring Boot, MyBatis, Git

---

## Task 1: 合并 feature/post-loan-management 分支

**Files:**
- None (Git 操作)

**Step 1: 检查当前分支状态**

```bash
git status
```

Expected: 当前在 main 分支，工作目录干净

**Step 2: 拉取远程最新代码**

```bash
git pull origin main
```

Expected: 成功拉取最新代码

**Step 3: 合并 feature/post-loan-management 分支**

```bash
git merge feature/post-loan-management -m "merge: 合并 feature/post-loan-management 分支"
```

Expected: 合并成功，无冲突

**Step 4: 推送到远程**

```bash
git push origin main
```

Expected: 成功推送到远程

---

## Task 2: 合并 feature/repayment-with-contracts 分支

**Files:**
- None (Git 操作)

**Step 1: 合并 feature/repayment-with-contracts 分支**

```bash
git merge feature/repayment-with-contracts -m "merge: 合并 feature/repayment-with-contracts 分支"
```

Expected: 合并成功，无冲突

**Step 2: 推送到远程**

```bash
git push origin main
```

Expected: 成功推送到远程

---

## Task 3: 删除已合并的 feature 分支

**Files:**
- None (Git 操作)

**Step 1: 删除本地 feature 分支**

```bash
git branch -d feature/post-loan-management
git branch -d feature/repayment-with-contracts
```

Expected: 本地分支删除成功

**Step 2: 删除远程 feature 分支**

```bash
git push origin --delete feature/post-loan-management
git push origin --delete feature/repayment-with-contracts
```

Expected: 远程分支删除成功（如果存在）

**Step 3: 验证分支状态**

```bash
git branch -a
```

Expected: 只剩下 main 和 develop 分支

---

## Task 4: 移动 Mapper 文件（第 1 批：CustomerMapper - SysPermissionMapper）

**Files:**
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/CustomerMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/CustomerMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/SysUserMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/SysUserMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/SysRoleMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/SysRoleMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/SysMenuMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/SysMenuMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/SysPermissionMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/SysPermissionMapper.java`

**Step 1: 移动文件**

```bash
cd backend/src/main/java/com/zerofinance/xwallet
git mv repository/CustomerMapper.java mapper/CustomerMapper.java
git mv repository/SysUserMapper.java mapper/SysUserMapper.java
git mv repository/SysRoleMapper.java mapper/SysRoleMapper.java
git mv repository/SysMenuMapper.java mapper/SysMenuMapper.java
git mv repository/SysPermissionMapper.java mapper/SysPermissionMapper.java
```

Expected: 5 个文件成功移动

**Step 2: 更新包声明**

对每个移动的文件，将第 1 行的：
```java
package com.zerofinance.xwallet.repository;
```
改为：
```java
package com.zerofinance.xwallet.mapper;
```

Expected: 5 个文件的包声明更新完成

**Step 3: 验证文件移动**

```bash
ls -la backend/src/main/java/com/zerofinance/xwallet/mapper/
```

Expected: 看到移动的 5 个文件

---

## Task 5: 移动 Mapper 文件（第 2 批：关联关系 Mapper）

**Files:**
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/SysRoleMenuMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/SysRoleMenuMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/SysRolePermissionMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/SysRolePermissionMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/SysUserRoleMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/SysUserRoleMapper.java`

**Step 1: 移动文件**

```bash
cd backend/src/main/java/com/zerofinance/xwallet
git mv repository/SysRoleMenuMapper.java mapper/SysRoleMenuMapper.java
git mv repository/SysRolePermissionMapper.java mapper/SysRolePermissionMapper.java
git mv repository/SysUserRoleMapper.java mapper/SysUserRoleMapper.java
```

Expected: 3 个文件成功移动

**Step 2: 更新包声明**

对每个移动的文件，将第 1 行的：
```java
package com.zerofinance.xwallet.repository;
```
改为：
```java
package com.zerofinance.xwallet.mapper;
```

Expected: 3 个文件的包声明更新完成

---

## Task 6: 移动 Mapper 文件（第 3 批：安全相关 Mapper）

**Files:**
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/TokenBlacklistMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/TokenBlacklistMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/VerificationCodeMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/VerificationCodeMapper.java`

**Step 1: 移动文件**

```bash
cd backend/src/main/java/com/zerofinance/xwallet
git mv repository/TokenBlacklistMapper.java mapper/TokenBlacklistMapper.java
git mv repository/VerificationCodeMapper.java mapper/VerificationCodeMapper.java
```

Expected: 2 个文件成功移动

**Step 2: 更新包声明**

对每个移动的文件，将第 1 行的：
```java
package com.zerofinance.xwallet.repository;
```
改为：
```java
package com.zerofinance.xwallet.mapper;
```

Expected: 2 个文件的包声明更新完成

---

## Task 7: 移动 Mapper 文件（第 4 批：贷款相关 Mapper）

**Files:**
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/LoanApplicationMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/LoanApplicationMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/LoanApplicationOtpMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/LoanApplicationOtpMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/LoanAccountMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/LoanAccountMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/LoanTransactionMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/LoanTransactionMapper.java`

**Step 1: 移动文件**

```bash
cd backend/src/main/java/com/zerofinance/xwallet
git mv repository/LoanApplicationMapper.java mapper/LoanApplicationMapper.java
git mv repository/LoanApplicationOtpMapper.java mapper/LoanApplicationOtpMapper.java
git mv repository/LoanAccountMapper.java mapper/LoanAccountMapper.java
git mv repository/LoanTransactionMapper.java mapper/LoanTransactionMapper.java
```

Expected: 4 个文件成功移动

**Step 2: 更新包声明**

对每个移动的文件，将第 1 行的：
```java
package com.zerofinance.xwallet.repository;
```
改为：
```java
package com.zerofinance.xwallet.mapper;
```

Expected: 4 个文件的包声明更新完成

---

## Task 8: 移动 Mapper 文件（第 5 批：合同与还款 Mapper）

**Files:**
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/LoanContractMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/LoanContractMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/LoanContractDocumentMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/LoanContractDocumentMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/RepaymentScheduleMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/RepaymentScheduleMapper.java`
- Move: `backend/src/main/java/com/zerofinance/xwallet/repository/AnalyticsEventMapper.java` → `backend/src/main/java/com/zerofinance/xwallet/mapper/AnalyticsEventMapper.java`

**Step 1: 移动文件**

```bash
cd backend/src/main/java/com/zerofinance/xwallet
git mv repository/LoanContractMapper.java mapper/LoanContractMapper.java
git mv repository/LoanContractDocumentMapper.java mapper/LoanContractDocumentMapper.java
git mv repository/RepaymentScheduleMapper.java mapper/RepaymentScheduleMapper.java
git mv repository/AnalyticsEventMapper.java mapper/AnalyticsEventMapper.java
```

Expected: 4 个文件成功移动

**Step 2: 更新包声明**

对每个移动的文件，将第 1 行的：
```java
package com.zerofinance.xwallet.repository;
```
改为：
```java
package com.zerofinance.xwallet.mapper;
```

Expected: 4 个文件的包声明更新完成

**Step 3: 验证所有文件已移动**

```bash
ls backend/src/main/java/com/zerofinance/xwallet/repository/*.java 2>/dev/null | wc -l
```

Expected: 输出 0（repository 目录下无 Java 文件）

---

## Task 9: 更新 Java 文件中的 import 语句

**Files:**
- Modify: 所有 `backend/src/main/java/**/*.java` 文件中的 import 语句

**Step 1: 查找需要更新的文件**

```bash
cd backend
grep -r "import com.zerofinance.xwallet.repository" src/main/java --include="*.java" | wc -l
```

Expected: 显示需要更新的 import 语句数量

**Step 2: 批量替换 import 语句**

```bash
cd backend
find src/main/java -name "*.java" -exec sed -i '' 's/import com\.zerofinance\.xwallet\.repository\./import com.zerofinance.xwallet.mapper./g' {} +
```

Expected: 所有 import 语句更新完成

**Step 3: 验证替换结果**

```bash
cd backend
grep -r "import com.zerofinance.xwallet.repository" src/main/java --include="*.java"
```

Expected: 无输出（所有 repository import 已替换）

---

## Task 10: 更新测试文件中的 import 语句

**Files:**
- Modify: 所有 `backend/src/test/java/**/*.java` 文件中的 import 语句

**Step 1: 批量替换测试文件 import 语句**

```bash
cd backend
find src/test/java -name "*.java" -exec sed -i '' 's/import com\.zerofinance\.xwallet\.repository\./import com.zerofinance.xwallet.mapper./g' {} +
```

Expected: 所有测试文件 import 语句更新完成

**Step 2: 验证替换结果**

```bash
cd backend
grep -r "import com.zerofinance.xwallet.repository" src/test/java --include="*.java"
```

Expected: 无输出（所有 repository import 已替换）

---

## Task 11: 更新 MyBatis XML 文件中的 namespace

**Files:**
- Modify: 所有 `backend/src/main/resources/mapper/*.xml` 文件

**Step 1: 查找需要更新的 XML 文件**

```bash
cd backend
grep -r "com.zerofinance.xwallet.repository" src/main/resources/mapper --include="*.xml" | wc -l
```

Expected: 显示需要更新的 namespace 数量

**Step 2: 批量替换 namespace**

```bash
cd backend
find src/main/resources/mapper -name "*.xml" -exec sed -i '' 's/com\.zerofinance\.xwallet\.repository\./com.zerofinance.xwallet.mapper./g' {} +
```

Expected: 所有 XML namespace 更新完成

**Step 3: 验证替换结果**

```bash
cd backend
grep -r "com.zerofinance.xwallet.repository" src/main/resources/mapper --include="*.xml"
```

Expected: 无输出（所有 repository namespace 已替换）

---

## Task 12: 更新启动类的 MapperScan 注解

**Files:**
- Modify: `backend/src/main/java/com/zerofinance/xwallet/XWalletBackendApplication.java:12`

**Step 1: 读取当前配置**

```bash
grep "@MapperScan" backend/src/main/java/com/zerofinance/xwallet/XWalletBackendApplication.java
```

Expected: 显示当前的双包扫描配置

**Step 2: 更新 MapperScan 注解**

将：
```java
@MapperScan({"com.zerofinance.xwallet.repository", "com.zerofinance.xwallet.mapper"})
```
改为：
```java
@MapperScan("com.zerofinance.xwallet.mapper")
```

Expected: 启动类更新完成

---

## Task 13: 删除空的 repository 目录

**Files:**
- Delete: `backend/src/main/java/com/zerofinance/xwallet/repository/`

**Step 1: 检查 repository 目录是否为空**

```bash
ls -la backend/src/main/java/com/zerofinance/xwallet/repository/
```

Expected: 只剩下 `.` 和 `..`（目录为空）

**Step 2: 删除空目录**

```bash
rmdir backend/src/main/java/com/zerofinance/xwallet/repository/
```

Expected: 空目录删除成功

**Step 3: 验证删除**

```bash
ls backend/src/main/java/com/zerofinance/xwallet/repository/ 2>&1
```

Expected: 输出 "No such file or directory"

---

## Task 14: 编译验证

**Files:**
- None (验证步骤)

**Step 1: 清理并编译**

```bash
cd backend
mvn clean compile
```

Expected: 编译成功，无错误

**Step 2: 检查编译输出**

查看是否有编译错误或警告，确保所有文件正确编译。

Expected: 编译通过，BUILD SUCCESS

---

## Task 15: 运行测试验证

**Files:**
- None (验证步骤)

**Step 1: 运行所有测试**

```bash
cd backend
mvn test
```

Expected: 所有测试通过

**Step 2: 检查测试结果**

确认测试覆盖率和测试通过率，确保重构未破坏现有功能。

Expected: 测试通过，BUILD SUCCESS

---

## Task 16: 提交重构变更

**Files:**
- None (Git 操作)

**Step 1: 查看所有变更**

```bash
git status
```

Expected: 显示所有移动和修改的文件

**Step 2: 暂存所有变更**

```bash
git add -A
```

Expected: 所有变更已暂存

**Step 3: 提交变更**

```bash
git commit -m "refactor: 统一 Mapper 包结构到 com.zerofinance.xwallet.mapper

- 将 repository 包下的 18 个 Mapper 接口移动到 mapper 包
- 更新所有 Java 文件的 import 语句
- 更新 MyBatis XML 文件的 namespace
- 简化启动类的 MapperScan 配置
- 删除空的 repository 目录"
```

Expected: 提交成功

**Step 4: 推送到远程**

```bash
git push origin main
```

Expected: 成功推送到远程

---

## Task 17: 最终验证

**Files:**
- None (验证步骤)

**Step 1: 验证项目结构**

```bash
ls -la backend/src/main/java/com/zerofinance/xwallet/mapper/
```

Expected: 看到所有 20 个 Mapper 文件（原有 2 个 + 迁移 18 个）

**Step 2: 验证无残留**

```bash
find backend -name "*.java" -exec grep -l "com.zerofinance.xwallet.repository" {} \;
find backend -name "*.xml" -exec grep -l "com.zerofinance.xwallet.repository" {} \;
```

Expected: 无输出（所有引用已更新）

**Step 3: 运行完整构建**

```bash
cd backend
mvn clean install
```

Expected: 构建成功，包括编译、测试、打包

---

## Success Criteria

- [ ] 所有 feature 分支已合并到 main
- [ ] 所有 feature 分支已删除
- [ ] 所有 Mapper 文件已移动到 mapper 包
- [ ] 所有 Java 文件的 import 语句已更新
- [ ] 所有 MyBatis XML 文件的 namespace 已更新
- [ ] 启动类的 MapperScan 注解已简化
- [ ] repository 目录已删除
- [ ] 编译成功
- [ ] 所有测试通过
- [ ] 变更已提交并推送到远程

---

## Rollback Plan

如果重构后发现问题，可以回滚到重构前的状态：

```bash
git reset --hard 99ade89  # 重构前的提交
git push origin main --force  # 强制推送（谨慎使用）
```
