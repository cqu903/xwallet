package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "交易备注更新请求")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanTransactionNoteUpdateRequest {

    @Schema(description = "备注内容")
    private String note;
}
