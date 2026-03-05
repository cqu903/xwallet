-- Create repayment_schedule table
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='还款计划表';

-- Create payment_record table
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='还款记录表';

-- Create payment_allocation table
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='还款分配明细表';
