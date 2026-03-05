package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.RepaymentScheduleResponse;
import com.zerofinance.xwallet.model.entity.RepaymentSchedule;
import com.zerofinance.xwallet.service.RepaymentScheduleService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 还款计划控制器
 */
@Tag(name = "还款计划管理", description = "还款计划查询、更新等操作")
@Slf4j
@RestController
@RequestMapping("/repayment/schedules")
@RequiredArgsConstructor
public class RepaymentScheduleController {

    private final RepaymentScheduleService scheduleService;

    @Operation(summary = "根据ID查询还款计划", description = "查询单个还款计划详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "还款计划不存在"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/{id}")
    public ResponseResult<RepaymentScheduleResponse> findById(@PathVariable Long id) {
        try {
            RepaymentSchedule schedule = scheduleService.findById(id);
            return ResponseResult.success(toResponse(schedule));
        } catch (IllegalArgumentException e) {
            log.warn("查询还款计划失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("查询还款计划失败", e);
            return ResponseResult.error(500, "查询还款计划失败");
        }
    }

    @Operation(summary = "根据贷款账户查询还款计划列表", description = "查询指定贷款账户的所有还款计划")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/loan-account/{loanAccountId}")
    public ResponseResult<List<RepaymentScheduleResponse>> findByLoanAccountId(@PathVariable Long loanAccountId) {
        try {
            List<RepaymentSchedule> schedules = scheduleService.findByLoanAccountId(loanAccountId);
            List<RepaymentScheduleResponse> responses = schedules.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return ResponseResult.success(responses);
        } catch (IllegalArgumentException e) {
            log.warn("查询还款计划列表失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("查询还款计划列表失败", e);
            return ResponseResult.error(500, "查询还款计划列表失败");
        }
    }

    @Operation(summary = "根据合同编号查询还款计划列表", description = "查询指定合同的所有还款计划")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/contract/{contractNumber}")
    public ResponseResult<List<RepaymentScheduleResponse>> findByContractNumber(@PathVariable String contractNumber) {
        try {
            List<RepaymentSchedule> schedules = scheduleService.findByContractNumber(contractNumber);
            List<RepaymentScheduleResponse> responses = schedules.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return ResponseResult.success(responses);
        } catch (IllegalArgumentException e) {
            log.warn("查询还款计划列表失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("查询还款计划列表失败", e);
            return ResponseResult.error(500, "查询还款计划列表失败");
        }
    }

    @Operation(summary = "查询逾期还款计划列表", description = "查询所有逾期的还款计划")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/overdue")
    public ResponseResult<List<RepaymentScheduleResponse>> findOverdueSchedules() {
        try {
            List<RepaymentSchedule> schedules = scheduleService.findOverdueSchedules();
            List<RepaymentScheduleResponse> responses = schedules.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return ResponseResult.success(responses);
        } catch (Exception e) {
            log.error("查询逾期还款计划列表失败", e);
            return ResponseResult.error(500, "查询逾期还款计划列表失败");
        }
    }

    private RepaymentScheduleResponse toResponse(RepaymentSchedule schedule) {
        BigDecimal remainingAmount = schedule.getTotalAmount()
                .subtract(schedule.getPaidPrincipal() != null ? schedule.getPaidPrincipal() : BigDecimal.ZERO)
                .subtract(schedule.getPaidInterest() != null ? schedule.getPaidInterest() : BigDecimal.ZERO);

        return RepaymentScheduleResponse.builder()
                .id(schedule.getId())
                .loanAccountId(schedule.getLoanAccountId())
                .contractNumber(schedule.getContractNumber())
                .installmentNumber(schedule.getInstallmentNumber())
                .dueDate(schedule.getDueDate())
                .principalAmount(schedule.getPrincipalAmount())
                .interestAmount(schedule.getInterestAmount())
                .totalAmount(schedule.getTotalAmount())
                .paidPrincipal(schedule.getPaidPrincipal())
                .paidInterest(schedule.getPaidInterest())
                .remainingAmount(remainingAmount)
                .status(schedule.getStatus().name())
                .createdAt(schedule.getCreatedAt() != null ? schedule.getCreatedAt().toString() : null)
                .updatedAt(schedule.getUpdatedAt() != null ? schedule.getUpdatedAt().toString() : null)
                .build();
    }
}
