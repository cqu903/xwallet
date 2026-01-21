package com.zerofinance.xwallet.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     * @param to 收件人邮箱
     * @param code 验证码
     * @param codeType 验证码类型
     */
    void sendVerificationEmail(String to, String code, String codeType);
}
