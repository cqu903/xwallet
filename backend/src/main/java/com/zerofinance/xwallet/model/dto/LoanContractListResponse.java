package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "贷款合同列表响应")
public class LoanContractListResponse {

    @Schema(description = "合同列表")
    private List<LoanContractSummaryResponse> contracts;

    @Schema(description = "合同数量")
    private Integer total;
}
