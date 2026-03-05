# 贷后管理系统设计文档

**文档版本：** 1.0  
**创建日期：** 2026-03-05  
**作者：** Claude & User  
**状态：** 已确认

---

## 1. 项目背景与目标

### 1.1 背景
xWallet 系统目前已实现基础的贷款申请、交易管理功能，但在贷后管理环节存在以下痛点：
- **还款计划不清晰**：用户和管理员无法查看详细的还款计划，不清楚每期应还金额
- **缺少风控规则配置**：缺乏自动化的风控规则配置，依赖人工判断
- **催收流程未系统化**：逾期客户无系统化跟踪，催收任务靠手动记录

### 1.2 目标
本次设计旨在增强系统的**业务流程能力**，重点解决：
1. **还款计划管理**：生成和展示详细的还款计划表，记录用户实际还款行为
2. **催收任务系统**：系统化管理催收流程，跟踪逾期账户

### 1.3 实施策略
采用**快速上线**策略，优先实现核心功能，保证可用性，后续迭代优化。

---

## 2. 业务模型

### 2.1 核心概念

#### 分期贷款模式（类似房贷）
- 每次贷款是一个独立的合同
- 一笔贷款还清后结束
- 催收是针对合同级别

#### 实体关系
```
customer (客户)
    ↓ 1:N
loan_account (贷款账户 - 每个合同一个账户)
    ↕ 1:1
loan_contract (合同 - 签署后不可变)
    
loan_account
    ↓ 1:N
repayment_schedule (还款计划 - 包含多期)
    
loan_account
    ↓ 1:1 (活跃时)
collection_task (催收任务)
```

### 2.2 关键业务规则

#### 账户管理
- **1个客户可以有多笔贷款** = 多个合同 + 多个账户
- **每个合同对应1个独立的贷款账户**
- **账户是贷后管理的起点**，管理状态、罚息率、逾期情况
- **合同签署后不可变**

#### 还款管理
- **还款计划**：每个账户有1个还款计划（业务概念），实际存储为多期记录
- **还款记录**：记录用户每次还款行为（还款计划 ≠ 还款记录）
- **还款分配**：一笔还款可能分配到多期（1条 payment_record → N条 payment_allocation）

#### 催收管理
- **催收任务生命周期**：可以关闭后重新生成（部分还款后再次逾期）
- **每个账户最多1个活跃催收任务**
- **逾期认定**：到期日次日算逾期第1天
- **罚息计算**：仅对逾期本金计算罚息（不含基础利息）

#### 定时任务
- **执行时间**：每天凌晨 00:10
- **执行内容**：
  1. 更新所有活跃催收任务的逾期天数、逾期金额
  2. 为新逾期账户生成催收任务
  3. 关闭已还清的催收任务

---

## 3. 数据库设计

### 3.1 表结构修改

#### 修改 `loan_account` 表
**新增字段：**
- `status` VARCHAR(20) - 账户状态（NORMAL/OVERDUE/FROZEN/CLOSED）
- `penalty_rate` DECIMAL(10,6) - 罚息率（日利率）
- `earliest_overdue_date` DATE - 最早逾期日期

**完整表结构：**
```sql
CREATE TABLE `loan_account` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `customer_id` BIGINT NOT NULL COMMENT '顾客ID',
    `credit_limit` DECIMAL(19,2) NOT NULL COMMENT '授信额度',
    `available_limit` DECIMAL(19,2) NOT NULL COMMENT '可用额度',
    `principal_outstanding` DECIMAL(19,2) NOT NULL COMMENT '在贷本金余额',
    `interest_outstanding` DECIMAL(19,2) NOT NULL COMMENT '应还利息余额',
    `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '账户状态: NORMAL/OVERDUE/FROZEN/CLOSED',
    `penalty_rate` DECIMAL(10,6) DEFAULT 0.0005 COMMENT '罚息率（日利率）',
    `earliest_overdue_date` DATE COMMENT '最早逾期日期',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号(并发控制)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_loan_account_customer` (`customer_id`),
    INDEX `idx_loan_account_customer` (`customer_id`),
    INDEX `idx_loan_account_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='贷款账户表';
```

---

### 3.2 新增表结构

#### 3.2.1 `repayment_schedule` - 还款计划表
```sql
CREATE TABLE repayment_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_account_id BIGINT NOT NULL COMMENT '贷款账户ID',
    contract_number VARCHAR(50) NOT NULL COMMENT '合同编号',
    installment_number INT NOT NULL COMMENT '期数（第N期）',
    due_date DATE NOT NULL COMMENT '到期日',
    principal_amount DECIMAL(15,2) NOT NULL COMMENT '本期应还本金',
    interest_amount DECIMAL(15,2) NOT NULL COMMENT '本期应还利息',
    total_amount DECIMAL(15,2) NOT NULL COMMENT '本期应还总额',
    paid_principal DECIMAL(15,2) DEFAULT 0 COMMENT '已还本金',
    paid_interest DECIMAL(15,2) DEFAULT 0 COMMENT '已还利息',
    status ENUM('PENDING', 'PARTIAL', 'PAID', 'OVERDUE') NOT NULL COMMENT '还款状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_account (loan_account_id),
    INDEX idx_contract_number (contract_number),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date),
    UNIQUE KEY uk_loan_installment (loan_account_id, installment_number)
) COMMENT='还款计划表';
```

#### 3.2.2 `payment_record` - 还款记录表
```sql
CREATE TABLE payment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_account_id BIGINT NOT NULL COMMENT '贷款账户ID',
    contract_number VARCHAR(50) NOT NULL COMMENT '合同编号',
    transaction_id BIGINT COMMENT '关联的交易ID（loan_transaction）',
    payment_amount DECIMAL(15,2) NOT NULL COMMENT '还款总金额',
    payment_time DATETIME NOT NULL COMMENT '用户还款时间',
    accounting_time DATETIME COMMENT '入账时间',
    payment_method ENUM('BANK_TRANSFER', 'AUTO_DEBIT', 'MANUAL', 'OTHER') COMMENT '还款方式',
    payment_source ENUM('APP', 'ADMIN', 'SYSTEM') NOT NULL COMMENT '还款来源',
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REVERSED') NOT NULL COMMENT '还款状态',
    reference_number VARCHAR(100) COMMENT '外部参考号（银行流水号等）',
    notes TEXT COMMENT '备注',
    operator_id BIGINT COMMENT '操作人ID（如果是后台录入）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_account (loan_account_id),
    INDEX idx_contract_number (contract_number),
    INDEX idx_payment_time (payment_time),
    INDEX idx_status (status),
    INDEX idx_transaction (transaction_id)
) COMMENT='还款记录表';
```

#### 3.2.3 `payment_allocation` - 还款分配明细表
```sql
CREATE TABLE payment_allocation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_record_id BIGINT NOT NULL COMMENT '还款记录ID',
    repayment_schedule_id BIGINT NOT NULL COMMENT '还款计划ID',
    installment_number INT NOT NULL COMMENT '期数',
    allocated_principal DECIMAL(15,2) NOT NULL COMMENT '分配到本金的金额',
    allocated_interest DECIMAL(15,2) NOT NULL COMMENT '分配到利息的金额',
    allocated_total DECIMAL(15,2) NOT NULL COMMENT '分配总额',
    allocation_rule ENUM('PRINCIPAL_FIRST', 'INTEREST_FIRST', 'PROPORTIONAL', 'MANUAL') COMMENT '分配规则',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_payment_record (payment_record_id),
    INDEX idx_repayment_schedule (repayment_schedule_id),
    INDEX idx_installment (installment_number)
) COMMENT='还款分配明细表';
```

#### 3.2.4 `collection_task` - 催收任务表
```sql
CREATE TABLE collection_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_account_id BIGINT NOT NULL COMMENT '贷款账户ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    contract_number VARCHAR(50) NOT NULL COMMENT '合同编号',
    overdue_days INT NOT NULL COMMENT '逾期天数',
    overdue_principal DECIMAL(15,2) NOT NULL COMMENT '逾期本金',
    overdue_interest DECIMAL(15,2) NOT NULL COMMENT '逾期利息（含罚息）',
    overdue_total DECIMAL(15,2) NOT NULL COMMENT '逾期总额',
    penalty_rate DECIMAL(10,6) DEFAULT 0.0005 COMMENT '罚息率（日利率）',
    last_calculated_at TIMESTAMP COMMENT '最后计算时间',
    status ENUM('PENDING', 'IN_PROGRESS', 'CONTACTED', 'PROMISED', 'PAID', 'CLOSED') NOT NULL COMMENT '催收状态',
    assigned_to BIGINT COMMENT '分配给的用户ID（催收员）',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM' COMMENT '优先级',
    last_contact_date DATE COMMENT '最后联系日期',
    next_contact_date DATE COMMENT '下次计划联系日期',
    promise_amount DECIMAL(15,2) COMMENT '承诺还款金额',
    promise_date DATE COMMENT '承诺还款日期',
    notes TEXT COMMENT '催收备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_account (loan_account_id),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_assigned_to (assigned_to),
    INDEX idx_priority (priority),
    INDEX idx_overdue_days (overdue_days),
    INDEX idx_next_contact (next_contact_date),
    INDEX idx_last_calculated (last_calculated_at)
) COMMENT='催收任务表';
```

#### 3.2.5 `collection_record` - 催收跟进记录表
```sql
CREATE TABLE collection_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    collection_task_id BIGINT NOT NULL COMMENT '催收任务ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    contact_method ENUM('PHONE', 'SMS', 'EMAIL', 'VISIT', 'OTHER') NOT NULL COMMENT '联系方式',
    contact_result ENUM('NO_ANSWER', 'PROMISED', 'REFUSED', 'UNREACHABLE', 'WRONG_NUMBER', 'OTHER') NOT NULL COMMENT '联系结果',
    contact_time DATETIME NOT NULL COMMENT '联系时间',
    notes TEXT COMMENT '联系备注',
    next_action VARCHAR(255) COMMENT '下一步行动',
    next_contact_date DATE COMMENT '下次计划联系日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_collection_task (collection_task_id),
    INDEX idx_operator (operator_id),
    INDEX idx_contact_time (contact_time)
) COMMENT='催收跟进记录表';
```

### 3.3 数据关系总结
- **还款模块**：`repayment_schedule` ← `payment_allocation` → `payment_record`
- **催收模块**：`collection_task` → `collection_record`
- **账户管理**：`loan_account`（新增状态、罚息率字段）

---

## 4. 后端 API 设计

### 4.1 还款计划管理

#### 4.1.1 查询还款计划列表
```
GET /api/admin/repayment/schedules
```
**参数：**
- `loanAccountId`: 账户ID（必填）
- `status`: 状态筛选（PENDING/PARTIAL/PAID/OVERDUE）
- `page`, `size`: 分页

**返回：**
- 分页列表（期数、到期日、应还本金、应还利息、已还本金、已还利息、状态）

**权限：** `repayment:schedule:view`

---

#### 4.1.2 查询还款记录列表
```
GET /api/admin/payment/records
```
**参数：**
- `loanAccountId`: 账户ID
- `contractNumber`: 合同号
- `status`: 状态（PENDING/SUCCESS/FAILED/REVERSED）
- `startDate`, `endDate`: 时间范围
- `page`, `size`: 分页

**返回：**
- 分页列表（还款时间、入账时间、金额、方式、状态）

**权限：** `payment:record:view`

---

#### 4.1.3 查询还款分配明细
```
GET /api/admin/payment/records/{recordId}/allocations
```
**返回：**
- 该笔还款分配到了哪些期数
- 每期的本金、利息分配金额

**权限：** `payment:record:view`

---

### 4.2 催收任务管理

#### 4.2.1 查询催收任务列表
```
GET /api/admin/collection/tasks
```
**参数：**
- `status`: 状态（PENDING/IN_PROGRESS/CONTACTED/PROMISED）
- `priority`: 优先级（LOW/MEDIUM/HIGH/URGENT）
- `assignedTo`: 催收员ID
- `overdueDaysMin`, `overdueDaysMax`: 逾期天数范围
- `page`, `size`: 分页

**返回：**
- 分页列表（客户信息、合同号、逾期金额、逾期天数、状态、优先级、下次联系日期）
- 统计：各状态数量

**权限：** `collection:task:view`

---

#### 4.2.2 查询催收任务详情
```
GET /api/admin/collection/tasks/{taskId}
```
**返回：**
- 任务基本信息
- 逾期金额明细（本金、基础利息、罚息）
- 客户信息
- 跟进记录时间线

**权限：** `collection:task:view`

---

#### 4.2.3 分配催收任务
```
PUT /api/admin/collection/tasks/{taskId}/assign
```
**参数：**
- `assignedTo`: 催收员ID

**权限：** `collection:task:assign`

---

#### 4.2.4 添加催收跟进记录
```
POST /api/admin/collection/tasks/{taskId}/records
```
**参数：**
- `contactMethod`: 联系方式
- `contactResult`: 联系结果
- `notes`: 备注
- `nextAction`: 下一步行动
- `nextContactDate`: 下次联系日期
- `promiseAmount`: 承诺金额（如果承诺还款）
- `promiseDate`: 承诺日期

**权限：** `collection:record:create`

---

#### 4.2.5 更新催收任务状态
```
PUT /api/admin/collection/tasks/{taskId}/status
```
**参数：**
- `status`: 新状态

**权限：** `collection:task:update`

---

### 4.3 贷款账户管理

#### 4.3.1 查询账户列表
```
GET /api/admin/loan/accounts
```
**参数：**
- `customerId`: 客户ID
- `status`: 账户状态（NORMAL/OVERDUE/FROZEN/CLOSED）
- `page`, `size`: 分页

**返回：**
- 分页列表（客户信息、合同号、授信额度、可用额度、本金余额、利息余额、状态、罚息率）

**权限：** `loan:account:view`

---

#### 4.3.2 查询账户详情
```
GET /api/admin/loan/accounts/{accountId}
```
**返回：**
- 账户基本信息
- 状态、罚息率
- 最早逾期日期
- 关联合同信息

**权限：** `loan:account:view`

---

#### 4.3.3 更新账户罚息率
```
PUT /api/admin/loan/accounts/{accountId}/penalty-rate
```
**参数：**
- `penaltyRate`: 新罚息率

**权限：** `loan:account:update`

---

#### 4.3.4 更新账户状态
```
PUT /api/admin/loan/accounts/{accountId}/status
```
**参数：**
- `status`: 新状态
- `reason`: 原因

**权限：** `loan:account:update`

---

### 4.4 定时任务（内部调度）

#### 每日更新催收任务
**执行时间：** 每天 00:10  
**实现方式：** Spring `@Scheduled` 注解  
**逻辑：**
1. 更新所有活跃任务的逾期天数、逾期金额
2. 为新逾期账户生成催收任务
3. 关闭已还清的任务

---

## 5. 前端页面设计

### 5.1 还款计划管理

#### 5.1.1 还款计划列表页
**路由：** `/post-loan/repayment-schedules`

**页面结构：**
- 搜索栏：账户搜索、状态筛选
- 账户信息卡片：客户、合同、状态、授信额度、本金余额
- 统计卡片：待还/部分还款/已还/逾期 期数统计
- 还款计划表格：期数、到期日、应还本金、应还利息、已还金额
- 操作按钮：查看还款记录

---

#### 5.1.2 还款记录列表页
**路由：** `/post-loan/payment-records`

**页面结构：**
- 搜索栏：合同号、时间范围、状态筛选、导出按钮
- 统计卡片：总还款金额、成功/失败笔数
- 还款记录表格：时间、金额、方式、状态、操作
- 详情按钮：查看还款分配明细

**还款记录详情弹窗：**
- 基本信息：还款金额、还款时间、入账时间、方式、状态
- 分配明细表格：期数、本金、利息、小计
- 分配规则说明

---

### 5.2 催收任务管理

#### 5.2.1 催收任务列表页
**路由：** `/post-loan/collection-tasks`

**页面结构：**
- 筛选栏：状态、优先级、催收员、逾期天数
- 统计卡片：待分配/进行中/已联系/承诺还款 数量
- 任务列表（按优先级、逾期天数排序）：
  - 客户、合同号、逾期天数、逾期金额
  - 状态、催收员、下次联系日期
  - 操作按钮：查看详情、添加跟进、重新分配

---

#### 5.2.2 催收任务详情页
**路由：** `/post-loan/collection-tasks/[id]`

**页面结构：**
- 基本信息：客户、电话、合同号
- 逾期情况：逾期天数、逾期本金、基础利息、罚息、逾期总额、数据更新时间
- 当前状态：状态、优先级、催收员、承诺信息
- 跟进记录时间线：联系时间、方式、结果、备注、下一步行动
- 操作按钮：添加跟进记录、重新分配、更新状态

**添加跟进记录弹窗：**
- 联系方式（下拉选择）
- 联系结果（下拉选择）
- 备注（文本域）
- 下一步行动（文本域）
- 下次联系日期（日期选择器）
- 承诺还款金额（数字输入，可选）
- 承诺还款日期（日期选择器，可选）

---

### 5.3 账户管理

#### 5.3.1 账户列表页
**路由：** `/post-loan/accounts`

**页面结构：**
- 搜索栏：客户ID、状态筛选
- 统计卡片：正常/逾期/冻结/已关闭 数量
- 账户表格：客户、合同号、授信额度、本金余额、状态
- 操作按钮：查看详情、调整罚息率、更新状态

**账户详情弹窗：**
- 基本信息：客户、合同、授信额度、可用额度、本金余额、利息余额
- 状态管理：当前状态、罚息率、最早逾期日期
- 快捷链接：查看还款计划、查看催收任务

---

## 6. 权限配置

### 6.1 新增菜单

#### 一级菜单：贷后管理
- 菜单名称：贷后管理
- 路由：NULL（容器菜单）
- 权限标识：`post-loan:view`
- 图标：ClipboardList
- 排序：30

#### 二级菜单
1. **还款计划**
   - 路由：`/post-loan/repayment-schedules`
   - 权限：`repayment:schedule:view`
   - 组件：`post-loan/repayment-schedules/index`

2. **还款记录**
   - 路由：`/post-loan/payment-records`
   - 权限：`payment:record:view`
   - 组件：`post-loan/payment-records/index`

3. **催收任务**
   - 路由：`/post-loan/collection-tasks`
   - 权限：`collection:task:view`
   - 组件：`post-loan/collection-tasks/index`

4. **账户管理**
   - 路由：`/post-loan/accounts`
   - 权限：`loan:account:view`
   - 组件：`post-loan/accounts/index`

---

### 6.2 新增按钮权限

#### 还款计划模块
- `repayment:schedule:view` - 查看还款计划
- `repayment:schedule:export` - 导出还款计划

#### 还款记录模块
- `payment:record:view` - 查看还款记录
- `payment:record:export` - 导出还款记录

#### 催收任务模块
- `collection:task:view` - 查看催收任务
- `collection:task:assign` - 分配催收任务
- `collection:task:update` - 更新催收任务状态
- `collection:record:create` - 添加跟进记录
- `collection:record:export` - 导出催收记录

#### 账户管理模块
- `loan:account:view` - 查看账户
- `loan:account:update` - 更新账户状态/罚息率
- `loan:account:export` - 导出账户列表

---

### 6.3 角色权限分配

#### ADMIN（超级管理员）
- ✅ 所有权限

#### OPERATOR（操作员）
- ✅ 查看还款计划
- ✅ 查看还款记录
- ✅ 查看催收任务
- ✅ 添加跟进记录
- ✅ 查看账户
- ❌ 分配催收任务
- ❌ 更新账户状态
- ❌ 导出功能

#### COLLECTOR（催收员 - 新角色）
- ✅ 查看催收任务
- ✅ 添加跟进记录
- ✅ 查看还款计划（只读）
- ✅ 查看还款记录（只读）
- ✅ 查看账户（只读）

#### VIEWER（查看员）
- ✅ 查看还款计划
- ✅ 查看还款记录
- ✅ 查看催收任务
- ✅ 查看账户

---

## 7. 实施计划

### 7.1 第一阶段：还款计划管理（2周）

#### Week 1：数据库 + 后端基础
- **Day 1-2**：数据库表设计与创建
  - 修改 `loan_account` 表（新增 status, penalty_rate, earliest_overdue_date）
  - 创建 `repayment_schedule` 表
  - 创建 `payment_record` 表
  - 创建 `payment_allocation` 表
  
- **Day 3-5**：后端 API 开发
  - RepaymentScheduleService（查询、生成还款计划）
  - PaymentRecordService（查询还款记录）
  - PaymentAllocationService（查询分配明细）
  - Controller 层接口实现

#### Week 2：前端页面 + 集成测试
- **Day 1-3**：前端页面开发
  - 还款计划列表页
  - 还款记录列表页
  - 还款记录详情弹窗
  
- **Day 4**：权限配置
  - 添加菜单数据
  - 配置权限标识
  - 分配角色权限
  
- **Day 5**：集成测试
  - 端到端测试
  - 数据一致性验证
  - Bug 修复

---

### 7.2 第二阶段：催收任务系统（2周）

#### Week 3：数据库 + 后端 API
- **Day 1**：数据库表创建
  - 创建 `collection_task` 表
  - 创建 `collection_record` 表
  
- **Day 2-4**：后端 API 开发
  - CollectionTaskService（CRUD、状态更新）
  - CollectionRecordService（添加跟进记录）
  - CollectionTaskScheduler（每日定时任务）
  - Controller 层接口实现
  
- **Day 5**：定时任务开发
  - 每日 00:10 更新逾期金额
  - 自动生成催收任务
  - 自动关闭已还清任务

#### Week 4：前端页面 + 集成测试
- **Day 1-3**：前端页面开发
  - 催收任务列表页
  - 催收任务详情页
  - 添加跟进记录弹窗
  
- **Day 4**：权限配置
  - 添加菜单数据
  - 配置权限标识
  - 创建 COLLECTOR 角色
  
- **Day 5**：集成测试
  - 端到端测试
  - 定时任务验证
  - Bug 修复

---

### 7.3 第三阶段：账户管理优化（1周）

- **Day 1-2**：账户管理前端页面
  - 账户列表页
  - 账户详情弹窗
  - 调整罚息率功能
  - 更新状态功能
  
- **Day 3**：集成与优化
  - 与还款计划、催收任务联动
  - 数据一致性验证
  
- **Day 4-5**：测试与上线
  - 全面回归测试
  - 性能测试
  - 文档编写
  - 上线部署

---

### 7.4 总计：5周

- ✅ 第一阶段（2周）：还款计划管理
- ✅ 第二阶段（2周）：催收任务系统
- ✅ 第三阶段（1周）：账户管理优化 + 测试上线

---

## 8. 技术要点

### 8.1 逾期金额计算逻辑
```
逾期本金 = SUM(每期应还本金 - 每期已还本金)
基础利息 = SUM(每期应还利息 - 每期已还利息)
罚息 = 逾期本金 × 罚息率 × 逾期天数
总逾期利息 = 基础利息 + 罚息
逾期总额 = 逾期本金 + 总逾期利息
```

### 8.2 优先级计算规则
- LOW: 逾期 1-30 天
- MEDIUM: 逾期 31-60 天
- HIGH: 逾期 61-90 天
- URGENT: 逾期 90+ 天

### 8.3 定时任务关键点
- 使用 Spring `@Scheduled(cron = "0 10 0 * * ?")`
- 事务管理：每个任务独立事务，失败不影响其他任务
- 日志记录：记录更新成功/失败数量
- 异常处理：捕获异常，避免影响其他任务

---

## 9. 风险与注意事项

### 9.1 数据迁移风险
- **风险**：现有 `loan_account` 表需要新增字段
- **缓解**：
  - 新增字段设置默认值
  - 分批更新现有数据
  - 提供回滚脚本

### 9.2 性能风险
- **风险**：定时任务处理大量逾期账户
- **缓解**：
  - 分批处理（每批100条）
  - 添加索引优化查询
  - 监控执行时间

### 9.3 业务理解风险
- **风险**：罚息计算规则、逾期认定规则可能与实际业务有偏差
- **缓解**：
  - 第一阶段完成后与业务确认
  - 预留配置化空间
  - 详细日志便于排查

---

## 10. 后续优化方向

### 10.1 功能增强
- 催收员工作量统计
- 催收效果分析报表
- 自动化催收提醒（短信/邮件）
- 还款计划生成规则配置化

### 10.2 技术优化
- 引入消息队列处理定时任务
- 添加缓存层优化查询性能
- 完善单元测试覆盖率
- 添加监控告警

---

## 11. 附录

### 11.1 相关文档
- [数据库初始化脚本](../backend/database/init_all.sql)
- [AGENTS.md](../../AGENTS.md)

### 11.2 变更历史
| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|----------|
| 1.0 | 2026-03-05 | Claude & User | 初始版本 |

---

**文档结束**
