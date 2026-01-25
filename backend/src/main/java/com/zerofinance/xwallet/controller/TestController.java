package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 仅用于开发/运维：生成 BCrypt 密码哈希等。生产环境建议关闭或加访问控制。
 */
@Tag(name = "测试", description = "开发辅助接口，如生成 BCrypt 密码；无需认证")
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "生成测试密码 BCrypt 哈希", description = "返回 admin123、customer123 的 BCrypt 及示例 UPDATE SQL，便于初始化或重置测试账号。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功") })
    @SecurityRequirements()
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
