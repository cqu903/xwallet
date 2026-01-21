package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 顾客Mapper接口
 */
@Mapper
public interface CustomerMapper {

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
}
