package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.entity.Customer;
import com.zerofinance.xwallet.model.entity.VerificationCode;
import com.zerofinance.xwallet.repository.CustomerMapper;
import com.zerofinance.xwallet.repository.VerificationCodeMapper;
import com.zerofinance.xwallet.service.EmailService;
import com.zerofinance.xwallet.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 验证码服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final VerificationCodeMapper verificationCodeMapper;
    private final CustomerMapper customerMapper;
    private final EmailService emailService;

    @Override
    public void sendVerificationCode(String email, String codeType) {
        log.info("发送验证码请求 - 邮箱: {}, 类型: {}", email, codeType);

        // 1. 验证邮箱格式
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        // 2. 如果是注册验证码，检查邮箱是否已注册
        if ("REGISTER".equals(codeType)) {
            if (!isEmailAvailable(email)) {
                throw new IllegalArgumentException("该邮箱已被注册");
            }
        }

        // 3. 防刷机制: 同一邮箱 60 秒内只能发送一次
        VerificationCode latestCode = verificationCodeMapper.findLatestByEmail(email, codeType);
        if (latestCode != null) {
            long secondsSinceLastSend = java.time.Duration.between(
                    latestCode.getCreatedAt(),
                    LocalDateTime.now()
            ).getSeconds();
            if (secondsSinceLastSend < 60) {
                throw new IllegalArgumentException("验证码发送过于频繁，请稍后再试");
            }
        }

        // 4. 生成 6 位随机数字验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 5. 保存验证码（5分钟有效）
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setCodeType(codeType);
        verificationCode.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        verificationCode.setVerified(false);
        verificationCode.setCreatedAt(LocalDateTime.now());

        verificationCodeMapper.save(verificationCode);

        // 6. 发送邮件
        emailService.sendVerificationEmail(email, code, codeType);

        log.info("验证码发送成功 - 邮箱: {}, 验证码: {}", email, code);
    }

    @Override
    public boolean verifyCode(String email, String code, String codeType) {
        log.info("验证验证码 - 邮箱: {}, 类型: {}", email, codeType);

        // 1. 查询最新的未验证验证码
        VerificationCode verificationCode = verificationCodeMapper.findLatestByEmail(email, codeType);
        if (verificationCode == null) {
            log.warn("验证码不存在 - 邮箱: {}", email);
            return false;
        }

        // 2. 检查是否已验证
        if (verificationCode.getVerified()) {
            log.warn("验证码已使用 - 邮箱: {}", email);
            return false;
        }

        // 3. 检查是否过期
        if (LocalDateTime.now().isAfter(verificationCode.getExpiredAt())) {
            log.warn("验证码已过期 - 邮箱: {}", email);
            return false;
        }

        // 4. 验证码匹配
        if (!verificationCode.getCode().equals(code)) {
            log.warn("验证码不匹配 - 邮箱: {}", email);
            return false;
        }

        // 5. 标记为已验证
        verificationCodeMapper.markAsVerified(verificationCode.getId());

        log.info("验证码验证成功 - 邮箱: {}", email);
        return true;
    }

    @Override
    public boolean isEmailAvailable(String email) {
        Customer existing = customerMapper.findByEmail(email);
        return existing == null;
    }
}
