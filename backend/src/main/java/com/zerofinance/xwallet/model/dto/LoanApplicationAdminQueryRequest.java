package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "贷款申请管理查询条件")
@Data
public class LoanApplicationAdminQueryRequest {

    @Schema(description = "申请编号")
    private String applicationNo;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "申请状态")
    private String status;

    @Schema(description = "风险决策")
    private String riskDecision;

    @Schema(description = "合同号")
    private String contractNo;

    @Schema(description = "合同状态")
    private String contractStatus;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "页码，从 1 开始", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer size = 10;
}
