package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.CreatePaymentRecordRequest;
import com.zerofinance.xwallet.model.dto.PaymentRecordResponse;
import com.zerofinance.xwallet.model.entity.PaymentRecord;
import com.zerofinance.xwallet.service.PaymentRecordService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 还款记录控制器
 */
@Tag(name = "还款记录管理", description = "还款记录查询、创建、确认等操作")
@Slf4j
@RestController
@RequestMapping("/payment/records")
@RequiredArgsConstructor
public class PaymentRecordController {

    private final PaymentRecordService paymentRecordService;

    @Operation(summary = "根据ID查询还款记录", description = "查询单个还款记录详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "还款记录不存在"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/{id}")
    public ResponseResult<PaymentRecordResponse> findById(@PathVariable Long id) {
        try {
            PaymentRecord record = paymentRecordService.findById(id);
            return ResponseResult.success(toResponse(record));
        } catch (IllegalArgumentException e) {
            log.warn("查询还款记录失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("查询还款记录失败", e);
            return ResponseResult.error(500, "查询还款记录失败");
        }
    }

    @Operation(summary = "根据贷款账户查询还款记录列表", description = "查询指定贷款账户的所有还款记录")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/loan-account/{loanAccountId}")
    public ResponseResult<List<PaymentRecordResponse>> findByLoanAccountId(@PathVariable Long loanAccountId) {
        try {
            List<PaymentRecord> records = paymentRecordService.findByLoanAccountId(loanAccountId);
            List<PaymentRecordResponse> responses = records.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return ResponseResult.success(responses);
        } catch (IllegalArgumentException e) {
            log.warn("查询还款记录列表失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("查询还款记录列表失败", e);
            return ResponseResult.error(500, "查询还款记录列表失败");
        }
    }

    @Operation(summary = "根据合同编号查询还款记录列表", description = "查询指定合同的所有还款记录")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/contract/{contractNumber}")
    public ResponseResult<List<PaymentRecordResponse>> findByContractNumber(@PathVariable String contractNumber) {
        try {
            List<PaymentRecord> records = paymentRecordService.findByContractNumber(contractNumber);
            List<PaymentRecordResponse> responses = records.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return ResponseResult.success(responses);
        } catch (IllegalArgumentException e) {
            log.warn("查询还款记录列表失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("查询还款记录列表失败", e);
            return ResponseResult.error(500, "查询还款记录列表失败");
        }
    }

    @Operation(summary = "创建还款记录", description = "创建新的还款记录（初始状态为PENDING）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PostMapping
    public ResponseResult<PaymentRecordResponse> createPaymentRecord(
            @Valid @RequestBody CreatePaymentRecordRequest request
    ) {
        try {
            PaymentRecord record = toEntity(request);
            PaymentRecord created = paymentRecordService.createPaymentRecord(record);
            return ResponseResult.success(toResponse(created));
        } catch (IllegalArgumentException e) {
            log.warn("创建还款记录失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建还款记录失败", e);
            return ResponseResult.error(500, "创建还款记录失败");
        }
    }

    @Operation(summary = "确认还款", description = "将还款记录状态更新为SUCCESS")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "409", description = "业务冲突"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PostMapping("/{id}/confirm")
    public ResponseResult<PaymentRecordResponse> confirmPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String operatorId
    ) {
        try {
            PaymentRecord confirmed = paymentRecordService.confirmPayment(id, operatorId);
            return ResponseResult.success(toResponse(confirmed));
        } catch (IllegalArgumentException e) {
            log.warn("确认还款失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("确认还款失败 - {}", e.getMessage());
            return ResponseResult.error(409, e.getMessage());
        } catch (Exception e) {
            log.error("确认还款失败", e);
            return ResponseResult.error(500, "确认还款失败");
        }
    }

    private PaymentRecord toEntity(CreatePaymentRecordRequest request) {
        PaymentRecord record = new PaymentRecord();
        record.setLoanAccountId(request.getLoanAccountId());
        record.setContractNumber(request.getContractNumber());
        record.setTransactionId(request.getTransactionId());
        record.setPaymentAmount(request.getPaymentAmount());
        record.setPaymentTime(request.getPaymentTime());
        record.setReferenceNumber(request.getReferenceNumber());
        record.setNotes(request.getNotes());
        
        if (request.getPaymentMethod() != null) {
            record.setPaymentMethod(PaymentRecord.PaymentMethod.valueOf(request.getPaymentMethod()));
        }
        if (request.getPaymentSource() != null) {
            record.setPaymentSource(PaymentRecord.PaymentSource.valueOf(request.getPaymentSource()));
        }
        
        return record;
    }

    private PaymentRecordResponse toResponse(PaymentRecord record) {
        return PaymentRecordResponse.builder()
                .id(record.getId())
                .loanAccountId(record.getLoanAccountId())
                .contractNumber(record.getContractNumber())
                .transactionId(record.getTransactionId())
                .paymentAmount(record.getPaymentAmount())
                .paymentTime(record.getPaymentTime())
                .accountingTime(record.getAccountingTime())
                .paymentMethod(record.getPaymentMethod() != null ? record.getPaymentMethod().name() : null)
                .paymentSource(record.getPaymentSource() != null ? record.getPaymentSource().name() : null)
                .status(record.getStatus().name())
                .referenceNumber(record.getReferenceNumber())
                .notes(record.getNotes())
                .operatorId(record.getOperatorId())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
