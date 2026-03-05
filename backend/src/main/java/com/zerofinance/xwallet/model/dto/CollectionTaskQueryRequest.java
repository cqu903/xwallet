package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "催收任务查询条件")
@Data
public class CollectionTaskQueryRequest {

    @Schema(description = "任务状态")
    private String status;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "负责人ID")
    private Long assignedTo;

    @Schema(description = "最小逾期天数")
    private Integer overdueDaysMin;

    @Schema(description = "最大逾期天数")
    private Integer overdueDaysMax;

    @Schema(description = "页码，从1开始", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "20")
    private Integer size = 20;
}
