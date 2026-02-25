package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.CustomerQueryRequest;
import com.zerofinance.xwallet.model.dto.CustomerResponse;
import com.zerofinance.xwallet.model.dto.PageResponse;

/**
 * 顾客服务接口
 */
public interface CustomerService {

    /**
     * 分页查询顾客列表
     * @param request 查询条件
     * @return 分页结果
     */
    PageResponse<CustomerResponse> getCustomerList(CustomerQueryRequest request);

    /**
     * 根据 ID 获取顾客详情
     * @param id 顾客 ID
     * @return 顾客详情
     * @throws IllegalArgumentException 顾客不存在时抛出
     */
    CustomerResponse getCustomerById(Long id);

    /**
     * 切换顾客状态（启用/禁用）
     * @param id 顾客 ID
     * @param status 状态：1-启用 0-禁用
     * @throws IllegalArgumentException 顾客不存在或状态值无效时抛出
     */
    void toggleCustomerStatus(Long id, Integer status);
}
