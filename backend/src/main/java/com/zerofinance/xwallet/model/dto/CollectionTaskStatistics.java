package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "催收任务统计信息")
@Data
public class CollectionTaskStatistics {

    @Schema(description = "待分配任务数")
    private long pending;

    @Schema(description = "进行中任务数")
    private long inProgress;

    @Schema(description = "已联系任务数")
    private long contacted;

    @Schema(description = "承诺还款任务数")
    private long promised;

    @Schema(description = "已还清任务数")
    private long paid;

    @Schema(description = "已关闭任务数")
    private long closed;

    @Schema(description = "总任务数")
    private long total;
}
