package com.zerofinance.xwallet.service;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {

    /**
     * 发送验证码
     * @param email 邮箱
     * @param codeType 验证码类型 (REGISTER, RESET_PASSWORD)
     */
    void sendVerificationCode(String email, String codeType);

    /**
     * 验证验证码
     * @param email 邮箱
     * @param code 验证码
     * @param codeType 验证码类型
     * @return true: 验证通过 false: 验证失败
     */
    boolean verifyCode(String email, String code, String codeType);

    /**
     * 验证邮箱唯一性
     * @param email 邮箱
     * @return true: 可用 false: 已存在
     */
    boolean isEmailAvailable(String email);
}
