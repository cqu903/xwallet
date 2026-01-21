package com.zerofinance.xwallet.annotation;

import java.lang.annotation.*;

/**
 * 角色校验注解
 * 用于快速校验用户角色
 *
 * 使用示例:
 * <pre>
 * {@code
 * @RequireRole("ADMIN")
 * @GetMapping("/system/config")
 * public ResponseResult getSystemConfig() { ... }
 *
 * // 需要拥有 ADMIN 或 SUPER_ADMIN 任一角色
 * @RequireRole(value = {"ADMIN", "SUPER_ADMIN"}, logical = Logical.OR)
 * @DeleteMapping("/users/{id}")
 * public ResponseResult deleteUser(@PathVariable Long id) { ... }
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 允许的角色编码
     */
    String[] value();

    /**
     * 验证模式
     * AND: 需要拥有所有角色
     * OR: 拥有任一角色即可 (默认)
     */
    RequirePermission.Logical logical() default RequirePermission.Logical.OR;

    /**
     * 验证失败时的提示信息
     */
    String message() default "角色权限不足";
}
