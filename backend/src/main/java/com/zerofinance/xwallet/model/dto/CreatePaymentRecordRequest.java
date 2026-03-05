package com.zerofinance.xwallet.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建还款记录请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRecordRequest {

    @NotNull(message = "贷款账户ID不能为空")
    @Positive(message = "贷款账户ID无效")
    private Long loanAccountId;

    @NotBlank(message = "合同编号不能为空")
    private String contractNumber;

    private Long transactionId;

    @NotNull(message = "还款金额不能为空")
    @DecimalMin(value = "0.01", message = "还款金额必须大于0")
    private BigDecimal paymentAmount;

    @NotNull(message = "还款时间不能为空")
    private LocalDateTime paymentTime;

    private String paymentMethod;

    @NotBlank(message = "还款来源不能为空")
    private String paymentSource;

    private String referenceNumber;
    private String notes;
}
