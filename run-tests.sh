#!/bin/bash

# 测试运行脚本
# 用于运行后端和前端的测试

set -e  # 遇到错误立即退出

echo "======================================"
echo "开始运行测试"
echo "======================================"
echo ""

# 1. 运行后端测试
echo "1️⃣  运行后端测试..."
cd /Users/royyuan/Downloads/codes/xwallet/backend
mvn test -Dtest=UserServiceTest

if [ $? -eq 0 ]; then
    echo "✅ 后端测试通过"
else
    echo "❌ 后端测试失败"
    exit 1
fi

echo ""
echo "======================================"
echo ""

# 2. 运行前端测试
echo "2️⃣  运行前端测试..."
cd /Users/royyuan/Downloads/codes/xwallet/front-web
pnpm test --passWithNoTests

if [ $? -eq 0 ]; then
    echo "✅ 前端测试通过"
else
    echo "❌ 前端测试失败"
    exit 1
fi

echo ""
echo "======================================"
echo "✅ 所有测试通过！"
echo "======================================"
