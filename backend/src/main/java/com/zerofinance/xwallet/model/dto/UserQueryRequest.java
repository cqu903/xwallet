package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户查询请求DTO（GET /user/list 的查询参数）
 */
@Schema(description = "用户列表查询条件")
@Data
public class UserQueryRequest {

    @Schema(description = "关键字，匹配工号或姓名")
    private String keyword;

    @Schema(description = "按角色 ID 筛选，可多选")
    private List<Long> roleIds;

    @Schema(description = "状态：1-启用 0-禁用，不传查全部")
    private Integer status;

    @Schema(description = "页码，从 1 开始", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer size = 10;
}
