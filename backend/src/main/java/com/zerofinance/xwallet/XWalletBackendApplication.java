package com.zerofinance.xwallet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * XWallet Backend Application
 * 钱包后端服务启动类
 */
@SpringBootApplication
@MapperScan("com.zerofinance.xwallet.repository")
public class XWalletBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(XWalletBackendApplication.class, args);
    }
}
