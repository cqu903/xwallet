package com.zerofinance.xwallet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger 配置
 * 提供接口文档与 Swagger UI，便于前端与第三方对接。
 *
 * <ul>
 *   <li>Swagger UI: /api/swagger-ui.html 或 /api/swagger-ui/index.html</li>
 *   <li>OpenAPI JSON: /api/v3/api-docs</li>
 *   <li>OpenAPI YAML: /api/v3/api-docs.yaml</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("登录后从 /auth/login 获取 token，填入时只需填写 token 字符串，不需要加 'Bearer ' 前缀。"
                                                        + " 需认证的接口请在 Swagger UI 右上角点击「Authorize」录入 token。")));
    }

    private Info apiInfo() {
        return new Info()
                .title("XWallet 后端 API")
                .description("xWallet 钱包后端服务 REST 接口。\n\n"
                        + "## 认证说明\n"
                        + "- **系统用户（管理后台）**：`userType=SYSTEM`，`account` 为工号，需配合 JWT 访问需权限接口。\n"
                        + "- **顾客（移动端）**：`userType=CUSTOMER`，`account` 为邮箱。\n\n"
                        + "## 通用响应格式\n"
                        + "`{ \"code\": 200, \"message\": \"success\", \"data\": ... }`，失败时 `code` 非 200，`data` 可为空。")
                .version("1.0.0")
                .contact(new Contact()
                        .name("XWallet")
                        .url("https://github.com/zerofinance/xwallet"))
                .license(new License().name("Proprietary").url(""));
    }

    private List<Server> servers() {
        Server local = new Server()
                .url("/api")
                .description("本地 / 默认（与 server.servlet.context-path 一致）");
        return List.of(local);
    }
}
