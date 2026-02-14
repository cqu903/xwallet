package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "贷款申请职业与财务信息")
public class LoanApplicationFinancialInfoRequest {

    @Schema(description = "职业", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "职业不能为空")
    private String occupation;

    @Schema(description = "月收入", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "月收入不能为空")
    @DecimalMin(value = "0.01", message = "月收入必须大于0")
    private BigDecimal monthlyIncome;

    @Schema(description = "每月应还负债", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "每月应还负债不能为空")
    @DecimalMin(value = "0", message = "每月应还负债不能小于0")
    private BigDecimal monthlyDebtPayment;
}
