package com.zerofinance.xwallet.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerofinance.xwallet.model.dto.LoginRequest;
import com.zerofinance.xwallet.model.dto.LoginResponse;
import com.zerofinance.xwallet.model.dto.MenuItemDTO;
import com.zerofinance.xwallet.util.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 菜单接口 E2E 测试
 * 
 * 测试完整的端到端流程：
 * 1. 用户登录获取 token
 * 2. 使用 token 访问菜单接口
 * 3. 验证返回的菜单数据
 * 
 * 注意：此测试需要数据库中有测试数据
 * 测试账号：ADMIN001 / admin123
 * 
 * 前置条件：
 * 1. MySQL 数据库已启动并包含测试数据
 * 2. 环境变量已设置（DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET等）
 *    或使用 .env 文件配置
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25",
    "spring.mail.username=test@example.com",
    "spring.mail.password=test",
    "mqtt.broker=tcp://localhost:1883",
    "mqtt.username=",
    "mqtt.password="
})
@DisplayName("菜单接口 E2E 测试")
@EnabledIfEnvironmentVariable(named = "RUN_E2E", matches = "true")
class MenuE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private String token;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
    }

    /**
     * 测试系统用户登录并获取菜单
     */
    @Test
    @DisplayName("系统用户登录并获取菜单列表")
    void testSystemUserLoginAndGetMenus() throws Exception {
        // 步骤1: 登录获取 token
        String loginUrl = baseUrl + "/auth/login";
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserType("SYSTEM");
        loginRequest.setAccount("ADMIN001");
        loginRequest.setPassword("admin123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> loginEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            loginUrl, loginEntity, String.class
        );

        // 验证登录响应
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());

        // 解析登录响应
        ResponseResult<LoginResponse> loginResult = objectMapper.readValue(
            loginResponse.getBody(),
            new TypeReference<ResponseResult<LoginResponse>>() {}
        );

        assertEquals(200, loginResult.getCode());
        assertNotNull(loginResult.getData());
        assertNotNull(loginResult.getData().getToken());
        assertNotNull(loginResult.getData().getUserInfo());
        assertEquals("SYSTEM", loginResult.getData().getUserInfo().getUserType());
        assertEquals("系统管理员", loginResult.getData().getUserInfo().getUsername());

        token = loginResult.getData().getToken();
        assertFalse(token.isEmpty(), "Token 不应该为空");

        // 步骤2: 使用 token 访问菜单接口
        String menusUrl = baseUrl + "/menus";
        
        HttpHeaders menuHeaders = new HttpHeaders();
        menuHeaders.setBearerAuth(token);
        menuHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> menuEntity = new HttpEntity<>(menuHeaders);

        ResponseEntity<String> menuResponse = restTemplate.exchange(
            menusUrl,
            HttpMethod.GET,
            menuEntity,
            String.class
        );

        // 验证菜单接口响应
        assertEquals(HttpStatus.OK, menuResponse.getStatusCode());
        assertNotNull(menuResponse.getBody());

        // 解析菜单响应
        ResponseResult<List<MenuItemDTO>> menuResult = objectMapper.readValue(
            menuResponse.getBody(),
            new TypeReference<ResponseResult<List<MenuItemDTO>>>() {}
        );

        assertEquals(200, menuResult.getCode());
        assertEquals("success", menuResult.getMessage());
        assertNotNull(menuResult.getData());
        assertFalse(menuResult.getData().isEmpty(), "菜单列表不应该为空");

        // 验证菜单数据结构
        List<MenuItemDTO> menus = menuResult.getData();
        assertTrue(menus.size() > 0, "至少应该有一个菜单项");

        // 验证菜单项的基本字段
        for (MenuItemDTO menu : menus) {
            assertNotNull(menu.getId(), "菜单ID不应该为空");
            assertNotNull(menu.getName(), "菜单名称不应该为空");
            // 顶级菜单（有子菜单的）可能没有 path，这是正常的
            if (menu.getChildren() == null || menu.getChildren().isEmpty()) {
                assertNotNull(menu.getPath(), "叶子菜单路径不应该为空");
            }
        }

        // 验证是否有系统管理菜单及其子菜单
        MenuItemDTO systemMenu = menus.stream()
            .filter(m -> "系统管理".equals(m.getName()))
            .findFirst()
            .orElse(null);

        assertNotNull(systemMenu, "应该存在系统管理菜单");
        assertNotNull(systemMenu.getChildren(), "系统管理菜单应该有子菜单");
        assertFalse(systemMenu.getChildren().isEmpty(), "系统管理菜单的子菜单不应该为空");

        // 验证子菜单结构
        List<MenuItemDTO> children = systemMenu.getChildren();
        assertTrue(children.size() >= 2, "系统管理菜单至少应该有2个子菜单");
        
        boolean hasMenuManagement = children.stream()
            .anyMatch(m -> "菜单管理".equals(m.getName()));
        boolean hasRoleManagement = children.stream()
            .anyMatch(m -> "角色管理".equals(m.getName()));
        
        assertTrue(hasMenuManagement, "应该有菜单管理子菜单");
        assertTrue(hasRoleManagement, "应该有角色管理子菜单");
    }

    /**
     * 测试未授权访问菜单接口
     */
    @Test
    @DisplayName("未授权访问菜单接口应该返回401")
    void testUnauthorizedAccess() {
        String menusUrl = baseUrl + "/menus";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            menusUrl,
            HttpMethod.GET,
            entity,
            String.class
        );

        // 应该返回401未授权
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * 测试无效 token 访问菜单接口
     */
    @Test
    @DisplayName("使用无效token访问菜单接口应该返回401")
    void testInvalidTokenAccess() {
        String menusUrl = baseUrl + "/menus";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid_token_12345");
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            menusUrl,
            HttpMethod.GET,
            entity,
            String.class
        );

        // 应该返回401未授权
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * 测试顾客用户登录（顾客用户不应该有菜单）
     */
    @Test
    @DisplayName("顾客用户登录并尝试获取菜单")
    void testCustomerLoginAndGetMenus() throws Exception {
        // 步骤1: 顾客登录
        String loginUrl = baseUrl + "/auth/login";
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserType("CUSTOMER");
        loginRequest.setAccount("customer@example.com");
        loginRequest.setPassword("customer123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> loginEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            loginUrl, loginEntity, String.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        
        ResponseResult<LoginResponse> loginResult = objectMapper.readValue(
            loginResponse.getBody(),
            new TypeReference<ResponseResult<LoginResponse>>() {}
        );

        assertEquals(200, loginResult.getCode());
        String customerToken = loginResult.getData().getToken();

        // 步骤2: 顾客访问菜单接口（可能返回空列表或错误）
        String menusUrl = baseUrl + "/menus";
        
        HttpHeaders menuHeaders = new HttpHeaders();
        menuHeaders.setBearerAuth(customerToken);
        menuHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> menuEntity = new HttpEntity<>(menuHeaders);

        ResponseEntity<String> menuResponse = restTemplate.exchange(
            menusUrl,
            HttpMethod.GET,
            menuEntity,
            String.class
        );

        // 顾客用户可能没有菜单权限，但接口应该能正常响应
        assertEquals(HttpStatus.OK, menuResponse.getStatusCode());
        
        ResponseResult<List<MenuItemDTO>> menuResult = objectMapper.readValue(
            menuResponse.getBody(),
            new TypeReference<ResponseResult<List<MenuItemDTO>>>() {}
        );

        assertEquals(200, menuResult.getCode());
        // 顾客用户的菜单列表可能为空，这是正常的
        assertNotNull(menuResult.getData());
    }
}
