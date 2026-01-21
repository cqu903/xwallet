package com.zerofinance.xwallet.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户查询请求DTO
 */
@Data
public class UserQueryRequest {

    /**
     * 关键字搜索（工号或姓名）
     */
    private String keyword;

    /**
     * 角色ID筛选
     */
    private List<Long> roleIds;

    /**
     * 状态筛选：1-启用 0-禁用 null-全部
     */
    private Integer status;

    /**
     * 页码，从1开始
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer size = 10;
}
