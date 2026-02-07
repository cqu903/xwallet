package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台交易记录项")
@Data
@Builder
public class LoanTransactionAdminItemResponse {

    @Schema(description = "交易号")
    private String transactionId;

    @Schema(description = "交易类型")
    private String type;

    @Schema(description = "交易状态")
    private String status;

    @Schema(description = "发生时间")
    private LocalDateTime occurredAt;

    @Schema(description = "交易金额")
    private BigDecimal amount;

    @Schema(description = "本金拆分")
    private BigDecimal principalComponent;

    @Schema(description = "利息拆分")
    private BigDecimal interestComponent;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "客户邮箱")
    private String customerEmail;

    @Schema(description = "合同号")
    private String contractId;

    @Schema(description = "幂等键")
    private String idempotencyKey;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "备注")
    private String note;

    @Schema(description = "冲正原交易号")
    private String reversalOf;
}
