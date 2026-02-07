package com.zerofinance.xwallet.util;

import com.zerofinance.xwallet.util.UserContext;
import com.zerofinance.xwallet.util.UserContext.UserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

/**
 * 测试基类 - 设置和清理测试环境
 */
public abstract class BaseTest {

    @BeforeEach
    void setUp() {
        beforeTest();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        afterTest();
    }

    /**
     * 测试前执行，子类可重写
     */
    protected void beforeTest() {
    }

    /**
     * 测试后执行，子类可重写
     */
    protected void afterTest() {
    }

    /**
     * 设置当前登录用户上下文
     */
    protected void setCurrentUser(Long userId, String username, String userType, String... roles) {
        UserInfo userInfo = new UserInfo(userId, username, userType, List.of(roles));
        UserContext.setUser(userInfo);
    }

    /**
     * 设置管理员用户上下文
     */
    protected void setAdminUser() {
        setCurrentUser(1L, "管理员", "SYSTEM", "ADMIN");
    }

    /**
     * 设置顾客用户上下文
     */
    protected void setCustomerUser() {
        setCurrentUser(100L, "测试顾客", "CUSTOMER");
    }
}
