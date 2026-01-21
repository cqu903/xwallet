package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.VerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 验证码Mapper接口
 */
@Mapper
public interface VerificationCodeMapper {

    /**
     * 保存验证码
     * @param verificationCode 验证码信息
     */
    void save(VerificationCode verificationCode);

    /**
     * 查询指定邮箱和类型的最新验证码
     * @param email 邮箱
     * @param codeType 验证码类型
     * @return 验证码信息
     */
    VerificationCode findLatestByEmail(@Param("email") String email, @Param("codeType") String codeType);

    /**
     * 标记验证码为已验证
     * @param id 验证码ID
     */
    void markAsVerified(@Param("id") Long id);

    /**
     * 删除过期的验证码
     */
    void deleteExpiredCodes();
}
