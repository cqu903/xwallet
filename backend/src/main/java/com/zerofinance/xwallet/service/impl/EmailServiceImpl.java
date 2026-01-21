package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.verification-code.expiration-minutes:5}")
    private int expirationMinutes;

    @Override
    public void sendVerificationEmail(String to, String code, String codeType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);

            String subject = "xWallet 验证码";
            String content = buildEmailContent(code, codeType);

            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("验证码邮件发送成功 - 邮箱: {}, 验证码: {}", to, code);
        } catch (Exception e) {
            log.error("发送验证码邮件失败 - 邮箱: {}", to, e);
            throw new RuntimeException("发送验证码失败，请稍后重试");
        }
    }

    private String buildEmailContent(String code, String codeType) {
        String typeName = "REGISTER".equals(codeType) ? "注册" : "重置密码";
        return String.format(
                "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                        "<h2 style='color: #2E7D32;'>xWallet %s验证码</h2>" +
                        "<p>您好，</p>" +
                        "<p>您正在进行 xWallet 账户%s操作，验证码如下：</p>" +
                        "<div style='background: #f5f5f5; padding: 20px; text-align: center; " +
                        "font-size: 32px; font-weight: bold; color: #2E7D32; " +
                        "letter-spacing: 5px; margin: 20px 0;'>%s</div>" +
                        "<p>验证码有效期为 <strong>%d 分钟</strong>，请尽快完成验证。</p>" +
                        "<p style='color: #666; font-size: 14px;'>如果这不是您的操作，请忽略此邮件。</p>" +
                        "</div>" +
                        "</body></html>",
                typeName, typeName, code, expirationMinutes
        );
    }
}
