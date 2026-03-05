-- Create collection_task table
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催收任务表';

-- Create collection_record table
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催收跟进记录表';
