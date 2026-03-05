# Phase 2 执行指南

**目标：** 在新会话中执行催收任务系统（Collection Task System）  
**预计时间：** 2周  
**前置条件：** Phase 1 已完成

---

## 📋 执行前准备

### 1. 确认环境状态

在当前会话运行以下命令确认环境：

```bash
# 检查数据库容器运行状态
docker ps | grep xwallet-mysql

# 检查数据库表是否存在
docker exec xwallet-mysql mysql -uroot -p123321qQ -e "
SELECT TABLE_NAME FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'xwallet' 
AND (TABLE_NAME LIKE '%payment%' OR TABLE_NAME LIKE '%repayment%');"

# 检查 loan_account 表新字段
docker exec xwallet-mysql mysql -uroot -p123321qQ -e "DESCRIBE xwallet.loan_account;"
```

**预期结果：**
- ✅ xwallet-mysql 容器运行中
- ✅ repayment_schedule, payment_record, payment_allocation 表存在
- ✅ loan_account 表有 status, penalty_rate, earliest_overdue_date 字段

### 2. 检查 Git 状态

```bash
# 查看当前分支
git branch

# 查看未提交的文件
git status

# 确保工作区干净
```

**预期结果：**
- 当前分支：main 或 feature/post-loan-management
- 工作区干净：nothing to commit, working tree clean

---

## 🚀 新会话执行步骤

### Step 1: 打开新终端

打开一个新的终端窗口或标签页。

### Step 2: 进入项目目录

```bash
cd /Users/admin/codes/xwallet
```

### Step 3: 启动新会话

在新会话中，复制粘贴以下提示词：

```
我需要继续执行贷后管理系统的 Phase 2 - 催收任务系统。

Phase 1 已完成，进度报告位于：
docs/plans/.progress/phase1-complete.md

请先读取这个进度文件了解已完成的工作。

然后使用 executing-plans skill 执行 Phase 2 计划：
docs/plans/2026-03-05-phase2-collection.md

请先执行前3个任务，完成后停止并报告。
```

### Step 4: 等待 AI 执行

AI 会：
1. 读取 Phase 1 完成报告
2. 读取 Phase 2 计划
3. 执行前 3 个任务（Task 1-3）
4. 创建数据库表、Entity 类、测试
5. 运行测试验证
6. **自动停止并报告完成情况**

### Step 5: 审查并继续

审查 AI 的完成报告后，输入：

```
继续执行下3个任务。
```

AI 会继续执行 Task 4-6，然后再次停止报告。

### Step 6: 重复直到完成

重复 Step 5，直到所有 Phase 2 任务完成。

---

## 📊 预期任务列表

Phase 2 包含以下任务（按执行顺序）：

### Week 3: 数据库 + 后端 API
- **Task 1:** 创建 collection_task 和 collection_record 表
- **Task 2:** 创建 CollectionTask Entity
- **Task 3:** 创建 CollectionRecord Entity
- **Task 4:** 创建 CollectionTaskMapper
- **Task 5:** 创建 CollectionRecordMapper
- **Task 6:** 创建 CollectionTaskService（基础 CRUD）
- **Task 7:** 创建 CollectionRecordService
- **Task 8:** 实现每日定时任务（00:10 更新逾期金额）
- **Task 9:** 实现自动生成催收任务逻辑
- **Task 10:** 实现自动关闭已还清任务逻辑

### Week 4: REST API + 前端
- **Task 11:** 创建 CollectionTaskController
- **Task 12:** 创建 REST API 端点（列表、详情、分配、状态更新）
- **Task 13:** 创建 CollectionRecordController
- **Task 14:** 创建添加跟进记录 API
- **Task 15:** 前端 - 催收任务列表页
- **Task 16:** 前端 - 催收任务详情页
- **Task 17:** 前端 - 添加跟进记录弹窗
- **Task 18:** 权限配置（菜单、按钮权限）
- **Task 19:** 集成测试
- **Task 20:** Bug 修复和优化

---

## ⚠️ 注意事项

### 1. 上下文管理

- **每执行 3 个任务**后，AI 会自动停止
- **审查完成后**，再继续下 3 个任务
- **如果上下文超过 70%**，考虑开启新会话

### 2. 遇到问题

如果 AI 遇到问题（测试失败、指令不清等），它会**自动停止并询问你**。

**不要强制继续**，先解决问题。

### 3. JAVA_HOME 配置

如果遇到 JAVA_HOME 错误：

```bash
# macOS (使用 Homebrew 安装的 Java 21)
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# 或者手动指定
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
```

### 4. 数据库连接

确保数据库容器运行：

```bash
# 检查状态
docker ps | grep xwallet-mysql

# 如果未运行，启动它
cd backend
docker-compose up -d mysql
```

---

## ✅ Phase 2 完成标准

Phase 2 完成后应该满足：

### 功能完整性
- ✅ 催收任务表和记录表已创建
- ✅ Entity、Repository、Service 层已实现
- ✅ 每日定时任务运行正常（00:10）
- ✅ REST API 端点可用
- ✅ 前端页面可以访问

### 测试通过
- ✅ Entity 单元测试通过
- ✅ Service 单元测试通过
- ✅ API 集成测试通过（如果有）

### 代码质量
- ✅ 代码已提交到 Git
- ✅ 提交信息清晰
- ✅ 符合项目规范（AGENTS.md）

### 权限配置
- ✅ 菜单数据已添加
- ✅ 权限标识已配置
- ✅ 角色权限已分配

---

## 📝 完成后的操作

### 1. 创建 Phase 2 完成报告

在新会话完成后，让 AI 创建：

```
docs/plans/.progress/phase2-complete.md
```

内容包括：
- 完成的任务列表
- Git commit hash
- 测试结果
- 遇到的问题和解决方案

### 2. Git Tag

创建 Phase 2 的 Git tag：

```bash
git tag -a v0.2.0-phase2 -m "Phase 2: Collection task system complete"
git push origin v0.2.0-phase2
```

### 3. 准备 Phase 3

Phase 3 将实现：
- 账户管理优化
- 罚息率调整功能
- 账户状态管理
- 最终集成测试

---

## 🔗 相关文档

- **Phase 1 完成报告**：`docs/plans/.progress/phase1-complete.md`
- **Phase 2 计划**：`docs/plans/2026-03-05-phase2-collection.md`
- **总体设计文档**：`docs/plans/2026-03-05-post-loan-management-design.md`
- **项目规范**：`AGENTS.md`

---

## 🆘 遇到问题？

如果执行过程中遇到问题：

1. **查看错误信息**：AI 会提供详细的错误描述
2. **检查环境**：确保数据库、Java 环境正常
3. **查看文档**：参考设计文档和 AGENTS.md
4. **回到这个会话**：我可以帮你诊断问题

---

**准备就绪？打开新终端，开始执行 Phase 2！** 🚀
