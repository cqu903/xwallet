package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台贷款申请记录项")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationAdminItemResponse {

    @Schema(description = "申请ID")
    private Long applicationId;

    @Schema(description = "申请编号")
    private String applicationNo;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "客户姓名")
    private String fullName;

    @Schema(description = "申请状态")
    private String status;

    @Schema(description = "风险决策")
    private String riskDecision;

    @Schema(description = "核准金额")
    private BigDecimal approvedAmount;

    @Schema(description = "合同号")
    private String contractNo;

    @Schema(description = "合同状态")
    private String contractStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
