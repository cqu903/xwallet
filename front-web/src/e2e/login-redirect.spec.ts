/**
 * 登录页面重定向 E2E 测试
 *
 * 测试目标:
 * 1. 验证访问首页 (/) 自动重定向到 /login
 * 2. 验证直接访问 /login 页面正常工作
 * 3. 验证重定向后的页面包含正确的登录表单元素
 * 4. 验证不同语言环境下的重定向行为
 */

import { test, expect } from '@playwright/test';

test.describe('登录页面重定向 E2E 测试', () => {
  test('应该将首页重定向到登录页面 (中文)', async ({ page }) => {
    // 访问首页
    await page.goto('http://localhost:3000/zh-CN');

    // 验证 URL 被重定向到 /login
    await page.waitForURL('**/zh-CN/login');
    expect(page.url()).toContain('/zh-CN/login');

    // 验证页面包含登录表单的关键元素
    await expect(page.locator('input[type="text"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
    await expect(page.getByText('登录')).toBeVisible();

    // 验证显示 xWallet 品牌标识
    await expect(page.getByText('xWallet')).toBeVisible();
  });

  test('应该将首页重定向到登录页面 (英文)', async ({ page }) => {
    // 访问英文首页
    await page.goto('http://localhost:3000/en-US');

    // 验证 URL 被重定向到 /login
    await page.waitForURL('**/en-US/login');
    expect(page.url()).toContain('/en-US/login');

    // 验证页面包含登录表单
    await expect(page.locator('input[type="text"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
  });

  test('应该能够直接访问登录页面而不重定向', async ({ page }) => {
    // 直接访问登录页面
    await page.goto('http://localhost:3000/zh-CN/login');

    // 等待页面加载完成
    await page.waitForLoadState('networkidle');

    // 验证 URL 保持为 /login (没有额外的重定向)
    expect(page.url()).toContain('/zh-CN/login');

    // 验证页面内容正常渲染
    await expect(page.locator('input[type="text"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
    await expect(page.getByText('钱包管理')).toBeVisible();
  });

  test('重定向后的登录页面应该显示测试账号信息', async ({ page }) => {
    // 访问首页触发重定向
    await page.goto('http://localhost:3000/zh-CN');
    await page.waitForURL('**/zh-CN/login');

    // 验证显示测试账号信息
    await expect(page.getByText('测试账号')).toBeVisible();
    await expect(page.getByText('ADMIN001')).toBeVisible();
    await expect(page.getByText('admin123')).toBeVisible();
  });

  test('重定向后的登录页面应该有主题切换按钮', async ({ page }) => {
    // 访问首页触发重定向
    await page.goto('http://localhost:3000/zh-CN');
    await page.waitForURL('**/zh-CN/login');

    // 验证主题切换按钮存在
    const themeToggle = page.locator('button[aria-label="切换主题"]');
    await expect(themeToggle).toBeVisible();
  });

  test('重定向后的登录页面应该显示专业双栏布局', async ({ page }) => {
    // 访问首页触发重定向
    await page.goto('http://localhost:3000/zh-CN');
    await page.waitForURL('**/zh-CN/login');

    // 验证左侧品牌区域可见 (桌面端)
    const brandSection = page.locator('.lg\\:flex');
    await expect(brandSection).toBeVisible();

    // 验证显示"后台系统"文字
    await expect(page.getByText('后台系统')).toBeVisible();
  });
});
