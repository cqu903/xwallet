package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "交易记录管理查询条件")
@Data
public class LoanTransactionAdminQueryRequest {

    @Schema(description = "客户邮箱")
    private String customerEmail;

    @Schema(description = "合同号")
    private String contractNo;

    @Schema(description = "交易类型")
    private String type;

    @Schema(description = "交易状态")
    private String status;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "幂等键")
    private String idempotencyKey;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "备注关键词")
    private String noteKeyword;

    @Schema(description = "金额下限")
    private BigDecimal amountMin;

    @Schema(description = "金额上限")
    private BigDecimal amountMax;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "页码，从 1 开始", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer size = 10;
}
