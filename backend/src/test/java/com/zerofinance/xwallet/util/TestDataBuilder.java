package com.zerofinance.xwallet.util;

import com.zerofinance.xwallet.model.entity.SysUser;
import com.zerofinance.xwallet.model.entity.SysRole;
import com.zerofinance.xwallet.model.entity.Customer;
import com.zerofinance.xwallet.model.entity.TokenBlacklist;

import java.time.LocalDateTime;

/**
 * 测试数据构建器
 * 用于创建测试对象，减少样板代码
 */
public class TestDataBuilder {

    public static SysUserBuilder sysUser() {
        return new SysUserBuilder();
    }

    public static SysRoleBuilder sysRole() {
        return new SysRoleBuilder();
    }

    public static CustomerBuilder customer() {
        return new CustomerBuilder();
    }

    public static TokenBlacklistBuilder tokenBlacklist() {
        return new TokenBlacklistBuilder();
    }

    /**
     * SysUser 构建器
     */
    public static class SysUserBuilder {
        private final SysUser user = new SysUser();

        public SysUserBuilder() {
            user.setId(1L);
            user.setEmployeeNo("TEST001");
            user.setUsername("测试用户");
            user.setEmail("test@example.com");
            user.setPassword("encoded_password");
            user.setStatus(1);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
        }

        public SysUserBuilder id(Long id) {
            user.setId(id);
            return this;
        }

        public SysUserBuilder employeeNo(String employeeNo) {
            user.setEmployeeNo(employeeNo);
            return this;
        }

        public SysUserBuilder username(String username) {
            user.setUsername(username);
            return this;
        }

        public SysUserBuilder email(String email) {
            user.setEmail(email);
            return this;
        }

        public SysUserBuilder password(String password) {
            user.setPassword(password);
            return this;
        }

        public SysUserBuilder status(Integer status) {
            user.setStatus(status);
            return this;
        }

        public SysUser build() {
            return user;
        }
    }

    /**
     * SysRole 构建器
     */
    public static class SysRoleBuilder {
        private final SysRole role = new SysRole();

        public SysRoleBuilder() {
            role.setId(1L);
            role.setRoleCode("ADMIN");
            role.setRoleName("管理员");
            role.setStatus(1);
        }

        public SysRoleBuilder id(Long id) {
            role.setId(id);
            return this;
        }

        public SysRoleBuilder roleCode(String roleCode) {
            role.setRoleCode(roleCode);
            return this;
        }

        public SysRoleBuilder roleName(String roleName) {
            role.setRoleName(roleName);
            return this;
        }

        public SysRoleBuilder status(Integer status) {
            role.setStatus(status);
            return this;
        }

        public SysRole build() {
            return role;
        }
    }

    /**
     * Customer 构建器
     */
    public static class CustomerBuilder {
        private final Customer customer = new Customer();

        public CustomerBuilder() {
            customer.setId(1L);
            customer.setEmail("customer@example.com");
            customer.setPassword("encoded_password");
            customer.setNickname("测试顾客");
            customer.setStatus(1);
            customer.setCreatedAt(LocalDateTime.now());
            customer.setUpdatedAt(LocalDateTime.now());
        }

        public CustomerBuilder id(Long id) {
            customer.setId(id);
            return this;
        }

        public CustomerBuilder email(String email) {
            customer.setEmail(email);
            return this;
        }

        public CustomerBuilder password(String password) {
            customer.setPassword(password);
            return this;
        }

        public CustomerBuilder nickname(String nickname) {
            customer.setNickname(nickname);
            return this;
        }

        public CustomerBuilder status(Integer status) {
            customer.setStatus(status);
            return this;
        }

        public Customer build() {
            return customer;
        }
    }

    /**
     * TokenBlacklist 构建器
     */
    public static class TokenBlacklistBuilder {
        private final TokenBlacklist tokenBlacklist = new TokenBlacklist();

        public TokenBlacklistBuilder() {
            tokenBlacklist.setId(1L);
            tokenBlacklist.setToken("test_token_12345");
            tokenBlacklist.setExpiryTime(LocalDateTime.now().plusDays(1));
            tokenBlacklist.setCreatedAt(LocalDateTime.now());
        }

        public TokenBlacklistBuilder id(Long id) {
            tokenBlacklist.setId(id);
            return this;
        }

        public TokenBlacklistBuilder token(String token) {
            tokenBlacklist.setToken(token);
            return this;
        }

        public TokenBlacklistBuilder expiryTime(LocalDateTime expiryTime) {
            tokenBlacklist.setExpiryTime(expiryTime);
            return this;
        }

        public TokenBlacklist build() {
            return tokenBlacklist;
        }
    }
}
