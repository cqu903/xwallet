package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "合同预览信息")
public class LoanContractPreviewResponse {

    @Schema(description = "合同号")
    private String contractNo;

    @Schema(description = "模板版本")
    private String templateVersion;

    @Schema(description = "合同内容")
    private String contractContent;
}
