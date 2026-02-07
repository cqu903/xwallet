package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台创建交易请求")
@Data
public class LoanTransactionAdminCreateRequest {

    @Schema(description = "客户邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerEmail;

    @Schema(description = "合同号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contractNo;

    @Schema(description = "交易类型：REPAYMENT / REDRAW_DISBURSEMENT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(description = "交易金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(description = "幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idempotencyKey;

    @Schema(description = "备注")
    private String note;
}
