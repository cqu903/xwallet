package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 顾客Mapper接口
 */
@Mapper
public interface CustomerMapper {

    /**
     * 根据ID查询顾客
     * @param id 顾客ID
     * @return 顾客信息
     */
    Customer findById(@Param("id") Long id);

    /**
     * 根据邮箱查询顾客
     * @param email 邮箱
     * @return 顾客信息
     */
    Customer findByEmail(@Param("email") String email);

    /**
     * 根据邮箱查询顾客（仅查询正常状态的顾客）
     * @param email 邮箱
     * @return 顾客信息
     */
    Customer findActiveByEmail(@Param("email") String email);

    /**
     * 插入新顾客
     * @param customer 顾客信息
     */
    void insert(Customer customer);

    /**
     * 分页查询顾客列表
     * @param keyword 关键词（匹配邮箱或昵称）
     * @param status 状态（1-启用 0-禁用，null表示全部）
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 顾客列表
     */
    List<Customer> findByPage(@Param("keyword") String keyword, @Param("status") Integer status, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 统计符合条件的顾客数量
     * @param keyword 关键词（匹配邮箱或昵称）
     * @param status 状态（1-启用 0-禁用，null表示全部）
     * @return 顾客数量
     */
    Integer countByCondition(@Param("keyword") String keyword, @Param("status") Integer status);

    /**
     * 更新顾客状态
     * @param id 顾客ID
     * @param status 状态（1-启用 0-禁用）
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
