package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "贷款申请职业选项")
public class LoanOccupationOptionResponse {

    @Schema(description = "职业编码")
    private String code;

    @Schema(description = "职业名称")
    private String label;
}
