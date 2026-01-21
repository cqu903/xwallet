package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于生成密码哈希
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 生成测试密码的BCrypt哈希
     * 访问: http://localhost:8080/api/test/password
     */
    @GetMapping("/password")
    public Map<String, String> generatePasswords() {
        Map<String, String> result = new HashMap<>();

        // 生成测试密码的哈希值
        String adminPassword = jwtUtil.encodePassword("admin123");
        String customerPassword = jwtUtil.encodePassword("customer123");

        result.put("admin123", adminPassword);
        result.put("customer123", customerPassword);

        result.put("adminUpdate", String.format(
            "UPDATE sys_user SET password = '%s' WHERE employee_no = 'ADMIN001';",
            adminPassword
        ));

        result.put("customerUpdate", String.format(
            "UPDATE customer SET password = '%s' WHERE email = 'customer@example.com';",
            customerPassword
        ));

        return result;
    }
}
