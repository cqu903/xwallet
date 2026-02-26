# 数据库中文乱码问题排查与解决

## 问题描述

后端 API 返回的中文数据（菜单名称、用户名等）在前端显示为乱码：
- `仪表盘` → `ä»ªè¡¨ç›˜`
- `系统管理` → `ç³»ç»Ÿç®¡ç`
- `系统管理员` → `ç³»ç»Ÿç®¡çå'˜`

## 根本原因

**双重编码问题**：UTF-8 数据被当作 Latin-1 (ISO-8859-1) 编码存储，导致：
1. 正确的 UTF-8 编码：`E4BBAAE8A1A8E79B98` (仪表盘)
2. 错误的双重编码：`C3A4C2BBC2AAC3A8C2A1C2A8C3A7E280BACB9C`

## 问题来源

### 1. 数据库初始化时缺少字符集参数

**错误做法**：
```bash
docker exec -i xwallet-mysql mysql -u root -p123321 q xwallet < init_all.sql
```

**正确做法**：
```bash
docker exec -i xwallet-mysql mysql -u root -p123321 q --default-character-set=utf8mb4 xwallet < init_all.sql
```

### 2. 数据库连接 URL 配置不完整

`backend/.env` 中的配置：

**错误**：
```
DB_URL=jdbc:mysql://localhost:3306/xwallet?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=utf8
```

**正确**：
```
DB_URL=jdbc:mysql://localhost:3306/xwallet?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8
```

## 排查步骤

### 1. 检查数据库中的实际编码
```bash
docker exec xwallet-mysql mysql -u root -p123321 q --default-character-set=utf8mb4 -e "
SELECT id, menu_name, HEX(menu_name) FROM xwallet.sys_menu WHERE id = 1;
"
```

- 正确的 `仪表盘` HEX: `E4BBAAE8A1A8E79B98`
- 错误的双重编码 HEX: `C3A4C2BBC2AAC3A8C2A1C2A8C3A7E280BACB9C`

### 2. 检查 API 返回数据
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d '{"userType":"SYSTEM","account":"ADMIN001","password":"admin123"}'
```

### 3. 检查数据库表字符集
```bash
docker exec xwallet-mysql mysql -u root -p123321 q -e "SHOW CREATE TABLE xwallet.sys_menu;"
```

预期结果：`DEFAULT CHARSET=utf8mb4`

## 解决方案

### 方案一：修复现有数据（不推荐）

如果只有少量数据乱码，可以手动修复：

```bash
docker exec xwallet-mysql mysql -u root -p123321 q --default-character-set=utf8mb4 xwallet << EOF
SET NAMES utf8mb4;
UPDATE sys_user SET username = '系统管理员' WHERE id = 1;
-- 修复其他表...
EOF
```

### 方案二：重新初始化数据库（推荐）

```bash
# 1. 删除并重建数据库
docker exec -i xwallet-mysql mysql -u root -p123321 q --default-character-set=utf8mb4 << EOF
DROP DATABASE IF EXISTS xwallet;
CREATE DATABASE xwallet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

# 2. 导入数据（必须带 --default-character-set=utf8mb4）
docker exec -i xwallet-mysql mysql -u root -p123321 q --default-character-set=utf8mb4 xwallet < backend/database/init_all.sql
```

## 经验教训

### 1. MySQL 字符集最佳实践

| 参数 | 推荐值 | 说明 |
|------|--------|------|
| 数据库字符集 | `utf8mb4` | 完整 UTF-8 支持，包括 emoji |
| 数据库排序规则 | `utf8mb4_unicode_ci` | 不区分大小写，Unicode 排序 |
| 连接字符集 | `UTF-8` | JDBC 连接参数 |
| 导入字符集 | `--default-character-set=utf8mb4` | mysql 命令行参数 |

### 2. 避免使用 `utf8`

MySQL 的 `utf8` 字符集实际上是 `utf8mb3`，最多支持 3 字节的 UTF-8，**不支持 emoji 等字符**。

- ❌ `characterEncoding=utf8` (仅 3 字节)
- ✅ `characterEncoding=UTF-8` (完整支持)

### 3. 数据库导入必须指定字符集

任何通过命令行导入 SQL 的操作，都必须加上 `--default-character-set=utf8mb4`：

```bash
# 正确
mysql --default-character-set=utf8mb4 -u root -p database < file.sql

# 错误（可能导致乱码）
mysql -u root -p database < file.sql
```

### 4. Docker MySQL 容器配置

确保 `docker-compose.yml` 中的环境变量正确：

```yaml
environment:
  - MYSQL_CHARACTER_SET_SERVER=utf8mb4
  - MYSQL_COLLATION_SERVER=utf8mb4_unicode_ci
```

## 检查清单

在部署新环境或排查乱码问题时，按以下顺序检查：

- [ ] 1. `docker-compose.yml` 中 MySQL 环境变量配置正确
- [ ] 2. `.env` 文件中 DB_URL 包含 `useUnicode=true&characterEncoding=UTF-8`
- [ ] 3. 导入 SQL 时使用了 `--default-character-set=utf8mb4`
- [ ] 4. 数据库表字符集为 `utf8mb4`
- [ ] 5. 数据库连接返回的 HEX 编码正确
- [ ] 6. API 返回的 JSON Content-Type 包含 `charset=UTF-8`

## 参考资料

- [MySQL 8.0 Character Set](https://dev.mysql.com/doc/refman/8.0/en/charset.html)
- [MySQL Connector/J Character Encoding](https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-charsets.html)
