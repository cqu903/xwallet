package com.zerofinance.xwallet.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 主要配置CORS跨域支持和拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final PermissionInterceptor permissionInterceptor;

    /**
     * 配置CORS跨域
     * 允许前端应用（Flutter Web和移动端）访问后端API
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 注册拦截器
     * 注意拦截器执行顺序:
     * 1. AuthInterceptor - 认证拦截器 (第一道关卡)
     * 2. PermissionInterceptor - 权限拦截器 (第二道关卡,在认证通过后执行)
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器(第一道关卡)
        // 注意：由于 context-path=/api，拦截器路径不需要包含 /api 前缀
        // 例如: /api/auth/login 在拦截器中匹配 /auth/login
        // 拦截器内部会通过 requestURI 判断是否需要认证
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**");

        // 权限拦截器(第二道关卡,在认证通过后执行)
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/test/**") // 排除不需要权限的路径
                .order(1); // 确保在 authInterceptor 之后执行
    }
}
