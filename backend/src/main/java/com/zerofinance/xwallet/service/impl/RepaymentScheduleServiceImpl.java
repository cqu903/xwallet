package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.entity.RepaymentSchedule;
import com.zerofinance.xwallet.mapper.RepaymentScheduleMapper;
import com.zerofinance.xwallet.service.RepaymentScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 还款计划服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepaymentScheduleServiceImpl implements RepaymentScheduleService {

    private final RepaymentScheduleMapper scheduleMapper;

    @Override
    public RepaymentSchedule findById(Long id) {
        log.info("查询还款计划 - id: {}", id);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("还款计划ID无效");
        }
        
        RepaymentSchedule schedule = scheduleMapper.findById(id);
        if (schedule == null) {
            throw new IllegalArgumentException("还款计划不存在: " + id);
        }
        
        return schedule;
    }

    @Override
    public List<RepaymentSchedule> findByLoanAccountId(Long loanAccountId) {
        log.info("查询贷款账户还款计划列表 - loanAccountId: {}", loanAccountId);
        if (loanAccountId == null || loanAccountId <= 0) {
            throw new IllegalArgumentException("贷款账户ID无效");
        }
        
        return scheduleMapper.findByLoanAccountId(loanAccountId);
    }

    @Override
    public List<RepaymentSchedule> findByContractNumber(String contractNumber) {
        log.info("查询合同还款计划列表 - contractNumber: {}", contractNumber);
        if (contractNumber == null || contractNumber.isBlank()) {
            throw new IllegalArgumentException("合同编号不能为空");
        }
        
        return scheduleMapper.findByContractNumber(contractNumber);
    }

    @Override
    @Transactional
    public RepaymentSchedule createSchedule(RepaymentSchedule schedule) {
        log.info("创建还款计划 - contractNumber: {}, installmentNumber: {}", 
                schedule.getContractNumber(), schedule.getInstallmentNumber());
        
        validateSchedule(schedule);
        
        LocalDateTime now = LocalDateTime.now();
        schedule.setCreatedAt(now);
        schedule.setUpdatedAt(now);
        
        if (schedule.getStatus() == null) {
            schedule.setStatus(RepaymentSchedule.ScheduleStatus.PENDING);
        }
        
        scheduleMapper.insert(schedule);
        log.info("还款计划创建成功 - id: {}", schedule.getId());
        
        return schedule;
    }

    @Override
    @Transactional
    public RepaymentSchedule updateSchedule(RepaymentSchedule schedule) {
        log.info("更新还款计划 - id: {}", schedule.getId());
        
        if (schedule.getId() == null || schedule.getId() <= 0) {
            throw new IllegalArgumentException("还款计划ID无效");
        }
        
        RepaymentSchedule existing = scheduleMapper.findById(schedule.getId());
        if (existing == null) {
            throw new IllegalArgumentException("还款计划不存在: " + schedule.getId());
        }
        
        int updated = scheduleMapper.update(schedule);
        if (updated == 0) {
            throw new RuntimeException("更新还款计划失败");
        }
        
        log.info("还款计划更新成功 - id: {}", schedule.getId());
        return scheduleMapper.findById(schedule.getId());
    }

    @Override
    public List<RepaymentSchedule> findByStatus(RepaymentSchedule.ScheduleStatus status) {
        log.info("查询还款计划列表 - status: {}", status);
        return scheduleMapper.findByStatus(status);
    }

    @Override
    public List<RepaymentSchedule> findOverdueSchedules() {
        log.info("查询逾期还款计划列表");
        return scheduleMapper.findByStatus(RepaymentSchedule.ScheduleStatus.OVERDUE);
    }

    private void validateSchedule(RepaymentSchedule schedule) {
        if (schedule.getLoanAccountId() == null || schedule.getLoanAccountId() <= 0) {
            throw new IllegalArgumentException("贷款账户ID无效");
        }
        if (schedule.getContractNumber() == null || schedule.getContractNumber().isBlank()) {
            throw new IllegalArgumentException("合同编号不能为空");
        }
        if (schedule.getInstallmentNumber() == null || schedule.getInstallmentNumber() <= 0) {
            throw new IllegalArgumentException("期数无效");
        }
        if (schedule.getDueDate() == null) {
            throw new IllegalArgumentException("到期日不能为空");
        }
        if (schedule.getPrincipalAmount() == null || schedule.getPrincipalAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("本金金额无效");
        }
        if (schedule.getInterestAmount() == null || schedule.getInterestAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("利息金额无效");
        }
        if (schedule.getTotalAmount() == null || schedule.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("总金额无效");
        }
    }
}
