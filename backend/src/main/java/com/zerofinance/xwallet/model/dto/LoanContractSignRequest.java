package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanContractSignRequest {
    @Schema(description = "合同号", required = true)
    @NotBlank(message = "合同号不能为空")
    private String contractNo;

    @Schema(description = "合同金额", required = true)
    @NotNull(message = "合同金额不能为空")
    @DecimalMin(value = "0.01", message = "合同金额必须大于0")
    private BigDecimal contractAmount;

    @Schema(description = "幂等键", required = true)
    @NotBlank(message = "幂等键不能为空")
    private String idempotencyKey;
}
