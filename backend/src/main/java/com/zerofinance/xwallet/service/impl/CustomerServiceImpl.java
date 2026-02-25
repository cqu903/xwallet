package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.dto.CustomerQueryRequest;
import com.zerofinance.xwallet.model.dto.CustomerResponse;
import com.zerofinance.xwallet.model.dto.PageResponse;
import com.zerofinance.xwallet.model.entity.Customer;
import com.zerofinance.xwallet.repository.CustomerMapper;
import com.zerofinance.xwallet.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 顾客服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final int MAX_PAGE_SIZE = 1000;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final CustomerMapper customerMapper;

    @Override
    public PageResponse<CustomerResponse> getCustomerList(CustomerQueryRequest request) {
        // 参数验证
        validatePageRequest(request);

        // 处理关键词（去除首尾空格，空字符串转为 null）
        String keyword = processKeyword(request.getKeyword());

        log.info("查询顾客列表 - keyword: {}, status: {}, page: {}, size: {}",
                keyword, request.getStatus(), request.getPage(), request.getSize());

        // 计算偏移量
        int offset = (request.getPage() - 1) * request.getSize();

        // 查询顾客列表
        List<Customer> customers = customerMapper.findByPage(
                keyword,
                request.getStatus(),
                offset,
                request.getSize()
        );

        // 统计总数
        int total = customerMapper.countByCondition(
                keyword,
                request.getStatus()
        );

        // 转换为响应 DTO
        List<CustomerResponse> customerResponses = customers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // 计算总页数（防止除以零）
        int totalPages = request.getSize() > 0
            ? (total + request.getSize() - 1) / request.getSize()
            : 0;

        return PageResponse.<CustomerResponse>builder()
                .content(customerResponses)
                .totalElements((long) total)
                .page(request.getPage())
                .size(request.getSize())
                .totalPages(totalPages)
                .build();
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        // 参数验证
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("顾客ID无效");
        }

        log.info("查询顾客详情 - id: {}", id);
        Customer customer = customerMapper.findById(id);
        if (customer == null) {
            throw new IllegalArgumentException("顾客不存在");
        }
        return convertToResponse(customer);
    }

    @Override
    @Transactional
    public void toggleCustomerStatus(Long id, Integer status) {
        // 参数验证
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("顾客ID无效");
        }

        log.info("更新顾客状态 - id: {}, status: {}", id, status);

        // 验证状态值
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态值无效，必须为 0（禁用）或 1（启用）");
        }

        // 检查顾客是否存在
        Customer customer = customerMapper.findById(id);
        if (customer == null) {
            throw new IllegalArgumentException("顾客不存在");
        }

        // 更新状态
        customerMapper.updateStatus(id, status);

        log.info("顾客状态更新成功 - id: {}, newStatus: {}", id, status);
    }

    /**
     * 验证分页请求参数
     * @param request 分页请求
     * @throws IllegalArgumentException 参数无效时抛出
     */
    private void validatePageRequest(CustomerQueryRequest request) {
        if (request.getPage() == null || request.getPage() < 1) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        if (request.getSize() == null || request.getSize() <= 0 || request.getSize() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("每页条数必须在1-" + MAX_PAGE_SIZE + "之间");
        }
    }

    /**
     * 处理关键词：去除首尾空格，空字符串转为 null
     * @param keyword 原始关键词
     * @return 处理后的关键词
     */
    private String processKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 将实体转换为响应 DTO
     * @param customer 顾客实体
     * @return 顾客响应 DTO
     */
    private CustomerResponse convertToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .nickname(customer.getNickname())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
