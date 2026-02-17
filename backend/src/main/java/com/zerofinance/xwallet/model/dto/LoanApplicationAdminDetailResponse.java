package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台贷款申请详情")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationAdminDetailResponse {

    @Schema(description = "申请ID")
    private Long applicationId;

    @Schema(description = "申请编号")
    private String applicationNo;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "申请状态")
    private String status;

    @Schema(description = "产品编码")
    private String productCode;

    @Schema(description = "核准金额")
    private BigDecimal approvedAmount;

    @Schema(description = "客户姓名")
    private String fullName;

    @Schema(description = "香港身份证号")
    private String hkid;

    @Schema(description = "家庭住址")
    private String homeAddress;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "职业")
    private String occupation;

    @Schema(description = "月收入")
    private BigDecimal monthlyIncome;

    @Schema(description = "月负债")
    private BigDecimal monthlyDebtPayment;

    @Schema(description = "风控决策")
    private String riskDecision;

    @Schema(description = "风控参考ID")
    private String riskReferenceId;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "冷却结束时间")
    private LocalDateTime cooldownUntil;

    @Schema(description = "审批通过时间")
    private LocalDateTime approvedAt;

    @Schema(description = "签署过期时间")
    private LocalDateTime expiresAt;

    @Schema(description = "签署完成时间")
    private LocalDateTime signedAt;

    @Schema(description = "放款完成时间")
    private LocalDateTime disbursedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "合同号")
    private String contractNo;

    @Schema(description = "模板版本")
    private String templateVersion;

    @Schema(description = "合同状态")
    private String contractStatus;

    @Schema(description = "合同摘要")
    private String digest;

    @Schema(description = "合同内容")
    private String contractContent;

    @Schema(description = "合同签署时间")
    private LocalDateTime contractSignedAt;
}
