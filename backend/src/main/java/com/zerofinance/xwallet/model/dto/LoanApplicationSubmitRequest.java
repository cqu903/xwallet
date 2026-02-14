package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "贷款申请提交请求")
public class LoanApplicationSubmitRequest {

    @Schema(description = "基础信息", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "基础信息不能为空")
    @Valid
    private LoanApplicationBasicInfoRequest basicInfo;

    @Schema(description = "职业与财务信息", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "财务信息不能为空")
    @Valid
    private LoanApplicationFinancialInfoRequest financialInfo;

    @Schema(description = "幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "幂等键不能为空")
    private String idempotencyKey;
}
