package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.entity.Customer;
import com.zerofinance.xwallet.model.entity.VerificationCode;
import com.zerofinance.xwallet.repository.CustomerMapper;
import com.zerofinance.xwallet.repository.VerificationCodeMapper;
import com.zerofinance.xwallet.service.impl.VerificationCodeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("验证码服务单元测试")
class VerificationCodeServiceTest {

    @Mock
    private VerificationCodeMapper verificationCodeMapper;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationCodeServiceImpl verificationCodeService;

    @Test
    @DisplayName("发送验证码邮箱格式非法")
    void testSendVerificationCodeInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> verificationCodeService.sendVerificationCode("bad-email", "REGISTER"));
    }

    @Test
    @DisplayName("注册邮箱已存在")
    void testSendVerificationCodeEmailExists() {
        when(customerMapper.findByEmail("u@example.com")).thenReturn(new Customer());

        assertThrows(IllegalArgumentException.class,
                () -> verificationCodeService.sendVerificationCode("u@example.com", "REGISTER"));
    }

    @Test
    @DisplayName("发送过于频繁")
    void testSendVerificationCodeTooFrequent() {
        when(customerMapper.findByEmail("u@example.com")).thenReturn(null);
        VerificationCode latest = new VerificationCode();
        latest.setCreatedAt(LocalDateTime.now().minusSeconds(30));
        when(verificationCodeMapper.findLatestByEmail("u@example.com", "REGISTER")).thenReturn(latest);

        assertThrows(IllegalArgumentException.class,
                () -> verificationCodeService.sendVerificationCode("u@example.com", "REGISTER"));
    }

    @Test
    @DisplayName("发送验证码成功")
    void testSendVerificationCodeSuccess() {
        when(customerMapper.findByEmail("u@example.com")).thenReturn(null);
        when(verificationCodeMapper.findLatestByEmail("u@example.com", "REGISTER")).thenReturn(null);
        doNothing().when(emailService).sendVerificationEmail(org.mockito.ArgumentMatchers.eq("u@example.com"),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq("REGISTER"));

        verificationCodeService.sendVerificationCode("u@example.com", "REGISTER");

        ArgumentCaptor<VerificationCode> captor = ArgumentCaptor.forClass(VerificationCode.class);
        verify(verificationCodeMapper).save(captor.capture());
        VerificationCode saved = captor.getValue();
        assertNotNull(saved.getCode());
        assertTrue(saved.getCode().matches("\\d{6}"));
        assertEquals("u@example.com", saved.getEmail());
        assertEquals("REGISTER", saved.getCodeType());
        verify(emailService).sendVerificationEmail(
                org.mockito.ArgumentMatchers.eq("u@example.com"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq("REGISTER")
        );
    }

    @Test
    @DisplayName("校验验证码不存在")
    void testVerifyCodeNotFound() {
        when(verificationCodeMapper.findLatestByEmail("u@example.com", "REGISTER")).thenReturn(null);

        boolean result = verificationCodeService.verifyCode("u@example.com", "123456", "REGISTER");

        assertFalse(result);
    }

    @Test
    @DisplayName("校验验证码已使用")
    void testVerifyCodeAlreadyUsed() {
        VerificationCode code = new VerificationCode();
        code.setVerified(true);
        when(verificationCodeMapper.findLatestByEmail("u@example.com", "REGISTER")).thenReturn(code);

        boolean result = verificationCodeService.verifyCode("u@example.com", "123456", "REGISTER");

        assertFalse(result);
    }

    @Test
    @DisplayName("校验验证码过期")
    void testVerifyCodeExpired() {
        VerificationCode code = new VerificationCode();
        code.setVerified(false);
        code.setExpiredAt(LocalDateTime.now().minusMinutes(1));
        when(verificationCodeMapper.findLatestByEmail("u@example.com", "REGISTER")).thenReturn(code);

        boolean result = verificationCodeService.verifyCode("u@example.com", "123456", "REGISTER");

        assertFalse(result);
    }

    @Test
    @DisplayName("校验验证码不匹配")
    void testVerifyCodeNotMatch() {
        VerificationCode code = new VerificationCode();
        code.setVerified(false);
        code.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        code.setCode("111111");
        when(verificationCodeMapper.findLatestByEmail("u@example.com", "REGISTER")).thenReturn(code);

        boolean result = verificationCodeService.verifyCode("u@example.com", "123456", "REGISTER");

        assertFalse(result);
    }

    @Test
    @DisplayName("校验验证码成功")
    void testVerifyCodeSuccess() {
        VerificationCode code = new VerificationCode();
        code.setId(1L);
        code.setVerified(false);
        code.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        code.setCode("123456");
        when(verificationCodeMapper.findLatestByEmail("u@example.com", "REGISTER")).thenReturn(code);

        boolean result = verificationCodeService.verifyCode("u@example.com", "123456", "REGISTER");

        assertTrue(result);
        verify(verificationCodeMapper).markAsVerified(1L);
    }

    @Test
    @DisplayName("邮箱可用性检查")
    void testIsEmailAvailable() {
        when(customerMapper.findByEmail("new@example.com")).thenReturn(null);
        when(customerMapper.findByEmail("used@example.com")).thenReturn(new Customer());

        assertTrue(verificationCodeService.isEmailAvailable("new@example.com"));
        assertFalse(verificationCodeService.isEmailAvailable("used@example.com"));
        verify(verificationCodeMapper, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private static void assertEquals(String expected, String actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
