package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("测试控制器单元测试")
class TestControllerTest {

    @Mock
    private JwtUtil jwtUtil;

    private TestController testController;

    @BeforeEach
    void setUp() {
        testController = new TestController();
        ReflectionTestUtils.setField(testController, "jwtUtil", jwtUtil);
    }

    @Test
    @DisplayName("生成测试密码与SQL")
    void testGeneratePasswords() {
        when(jwtUtil.encodePassword("admin123")).thenReturn("hash_admin");
        when(jwtUtil.encodePassword("customer123")).thenReturn("hash_customer");

        Map<String, String> result = testController.generatePasswords();

        assertEquals("hash_admin", result.get("admin123"));
        assertEquals("hash_customer", result.get("customer123"));
        assertTrue(result.get("adminUpdate").contains("hash_admin"));
        assertTrue(result.get("adminUpdate").contains("ADMIN001"));
        assertTrue(result.get("customerUpdate").contains("hash_customer"));
        assertTrue(result.get("customerUpdate").contains("customer@example.com"));
        verify(jwtUtil, times(1)).encodePassword("admin123");
        verify(jwtUtil, times(1)).encodePassword("customer123");
    }
}
