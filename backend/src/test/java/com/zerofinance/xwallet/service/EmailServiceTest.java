package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.service.impl.EmailServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("邮件服务单元测试")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@xwallet.com");
        ReflectionTestUtils.setField(emailService, "expirationMinutes", 8);
    }

    @Test
    @DisplayName("发送验证码邮件成功")
    void testSendVerificationEmailSuccess() {
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendVerificationEmail("u@example.com", "123456", "REGISTER");

        verify(mailSender).send(message);
    }

    @Test
    @DisplayName("发送验证码邮件失败抛业务异常")
    void testSendVerificationEmailFailure() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("smtp down"));

        assertThrows(RuntimeException.class,
                () -> emailService.sendVerificationEmail("u@example.com", "123456", "REGISTER"));
    }

    @Test
    @DisplayName("发送重置密码验证码邮件失败")
    void testSendResetPasswordEmailFailure() {
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        doThrow(new RuntimeException("send failed")).when(mailSender).send(message);

        assertThrows(RuntimeException.class,
                () -> emailService.sendVerificationEmail("u@example.com", "654321", "RESET_PASSWORD"));
    }
}
