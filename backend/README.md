# XWallet Backend

XWallet 后端 API 服务器，基于 Spring Boot 3.x + MyBatis。

## 相关环境
- Maven：Maven的安装路径可以在.bashrc中寻找，不要直接运行mvn命令
- Mysql：Mysql安装在docker里面，如果需要执行sql语句，应该通过docker连接到mysql容器再执行

## 技术栈

- Java 17
- Spring Boot 3.3.0
- MyBatis 3.0.3
- MySQL 8.x
- Maven

## 项目结构

```
backend/
├── src/main/java/com/zerofinance/xwallet/
│   ├── XWalletBackendApplication.java    # 启动类
│   ├── controller/                       # 控制器层
│   ├── service/                          # 服务层
│   ├── repository/                       # MyBatis Mapper 接口
│   ├── model/                            # 数据模型
│   │   ├── entity/                       # 数据库实体
│   │   └── dto/                          # 数据传输对象
│   └── config/                           # 配置类
├── src/main/resources/
│   ├── application.yml                   # 主配置
│   ├── application-dev.yml               # 开发环境配置
│   ├── logback-spring.xml                # 日志配置
│   └── mapper/                           # MyBatis XML 映射
└── pom.xml                               # Maven 配置
```

## 快速开始

### 前置要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 配置数据库和环境变量

1. 创建数据库：
```sql
CREATE DATABASE xwallet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 配置环境变量：

项目使用 **spring-dotenv** 从 `.env` 文件自动加载环境变量。

**创建 `.env` 文件：**
```bash
cd backend
vim .env
```

**示例配置：**
```bash
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/xwallet?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=utf8
DB_USERNAME=root
DB_PASSWORD=your_password_here

# 邮件配置
MAIL_HOST=smtp.exmail.qq.com
MAIL_PORT=465
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_password

# JWT配置
JWT_SECRET=your_jwt_secret_key_here

# MQTT配置（可选）
MQTT_BROKER_HOST=broker.emqxsl.com
MQTT_USERNAME=your_mqtt_username
MQTT_PASSWORD=your_mqtt_password
```

**重要说明：**
- spring-dotenv 会从 backend/ 目录加载 .env 文件
- 修改配置后需要重启后端才能生效
- `.env` 文件已加入 `.gitignore`，不会提交到版本控制

### 构建和运行

```bash
# 进入 backend 目录
cd backend

# 构建项目
mvn clean install

# 运行应用（使用 dev profile）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或者打包后运行
mvn clean package -DskipTests
java -jar target/xwallet-backend-1.0.0.jar --spring.profiles.active=dev
```

### 访问

应用启动后，访问：`http://localhost:8080/api`

## 开发指南

### 添加新功能

1. 在 `model/entity` 中创建实体类
2. 在 `repository` 中创建 MyBatis Mapper 接口
3. 在 `resources/mapper` 中创建 MyBatis XML 映射文件
4. 在 `service` 中创建服务类
5. 在 `controller` 中创建控制器

### 依赖管理

主要依赖版本在 `pom.xml` 的 `<properties>` 中定义。

## 许可证

[待定]
