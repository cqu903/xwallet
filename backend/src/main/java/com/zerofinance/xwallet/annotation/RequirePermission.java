package com.zerofinance.xwallet.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于 Controller 方法上,校验用户是否有指定权限
 *
 * 使用示例:
 * <pre>
 * {@code
 * @RequirePermission("user:create")
 * @PostMapping("/users")
 * public ResponseResult createUser(@RequestBody UserDTO user) { ... }
 *
 * // 需要 user:add 和 user:edit 两个权限 (AND 模式)
 * @RequirePermission(value = {"user:add", "user:edit"}, logical = Logical.AND)
 * @PostMapping("/users/batch")
 * public ResponseResult batchOperation() { ... }
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 需要的权限标识(支持多个)
     * 多个权限之间的逻辑关系由 logical 参数决定
     */
    String[] value() default {};

    /**
     * 权限验证模式
     * AND: 需要拥有所有权限
     * OR: 拥有任一权限即可 (默认)
     */
    Logical logical() default Logical.OR;

    /**
     * 权限验证失败时的提示信息
     */
    String message() default "权限不足";

    /**
     * 逻辑类型枚举
     */
    enum Logical {
        /** 需要拥有所有权限 */
        AND,
        /** 拥有任一权限即可 */
        OR
    }
}
