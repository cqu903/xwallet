/**
 * 主题切换 E2E 测试
 *
 * 测试目标:
 * 1. 验证页面加载时有正确的初始主题
 * 2. 验证点击主题切换按钮后,class 正确切换
 * 3. 验证 CSS 变量在主题切换后正确更新
 * 4. 验证视觉效果在主题切换后发生变化
 */

import { test, expect } from '@playwright/test';

test.describe('主题切换 E2E 测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:3000/en');
  });

  test('应该在初始加载时显示正确的主题', async ({ page }) => {
    // 等待页面加载完成
    await page.waitForLoadState('networkidle');

    // 获取 HTML 元素的 class
    const htmlClass = await page.locator('html').getAttribute('class');
    console.log('Initial HTML class:', htmlClass);

    // 验证初始状态(可能是 light 或 dark,取决于系统设置)
    expect(htmlClass === null || htmlClass === '' || htmlClass === 'dark').toBeTruthy();
  });

  test('应该在点击切换按钮后切换 dark class', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // 获取初始 class 状态
    const initialClass = await page.locator('html').getAttribute('class');
    console.log('Initial class:', initialClass);

    // 点击主题切换按钮
    const toggleButton = page.locator('button[aria-label="切换主题"]');
    await toggleButton.click();

    // 等待 DOM 更新
    await page.waitForTimeout(100);

    // 获取切换后的 class
    const newClass = await page.locator('html').getAttribute('class');
    console.log('New class:', newClass);

    // 验证 class 发生了变化
    expect(newClass).not.toBe(initialClass);

    // 如果初始没有 dark class,切换后应该有
    if (!initialClass?.includes('dark')) {
      expect(newClass).toContain('dark');
    } else {
      expect(newClass).not.toContain('dark');
    }
  });

  test('应该在切换主题后更新 CSS 变量', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // 获取初始背景色变量
    const initialBg = await page.evaluate(() => {
      const html = document.documentElement;
      return getComputedStyle(html).getPropertyValue('--color-background').trim();
    });
    console.log('Initial background:', initialBg);

    // 点击切换按钮
    const toggleButton = page.locator('button[aria-label="切换主题"]');
    await toggleButton.click();
    await page.waitForTimeout(100);

    // 获取切换后的背景色变量
    const newBg = await page.evaluate(() => {
      const html = document.documentElement;
      return getComputedStyle(html).getPropertyValue('--color-background').trim();
    });
    console.log('New background:', newBg);

    // 验证背景色变量发生了变化
    expect(newBg).not.toBe(initialBg);

    // 验证新值符合预期
    const hasDarkClass = await page.locator('html').classList.contains('dark');
    if (hasDarkClass) {
      expect(newBg).toContain('11%'); // dark 模式的背景色
    } else {
      expect(newBg).toContain('98.5%'); // light 模式的背景色
    }
  });

  test('应该在切换主题后产生视觉变化', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // 获取初始 body 背景色
    const initialBodyBg = await page.evaluate(() => {
      return getComputedStyle(document.body).backgroundColor;
    });
    console.log('Initial body background:', initialBodyBg);

    // 点击切换到 dark 模式
    const toggleButton = page.locator('button[aria-label="切换主题"]');
    await toggleButton.click();
    await page.waitForTimeout(100);

    // 获取 dark 模式下的 body 背景色
    const darkBodyBg = await page.evaluate(() => {
      return getComputedStyle(document.body).backgroundColor;
    });
    console.log('Dark body background:', darkBodyBg);

    // 验证背景色发生了变化
    expect(darkBodyBg).not.toBe(initialBodyBg);

    // 解析 RGB 值并验证暗色模式确实更暗
    const rgbMatch = darkBodyBg.match(/rgb\((\d+),\s*(\d+),\s*(\d+)\)/);
    if (rgbMatch) {
      const [, r, g, b] = rgbMatch.map(Number);
      const brightness = (r + g + b) / 3;
      console.log('Dark mode brightness:', brightness);
      // 暗色模式的亮度应该较低
      expect(brightness).toBeLessThan(128);
    }
  });

  test('应该支持多次切换主题', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    const toggleButton = page.locator('button[aria-label="切换主题"]');

    // 第一次切换
    await toggleButton.click();
    await page.waitForTimeout(100);
    let class1 = await page.locator('html').getAttribute('class');
    console.log('After first click:', class1);

    // 第二次切换
    await toggleButton.click();
    await page.waitForTimeout(100);
    let class2 = await page.locator('html').getAttribute('class');
    console.log('After second click:', class2);

    // 第三次切换
    await toggleButton.click();
    await page.waitForTimeout(100);
    let class3 = await page.locator('html').getAttribute('class');
    console.log('After third click:', class3);

    // 验证每次切换都会改变 class
    expect(class2).not.toBe(class1);
    expect(class3).not.toBe(class2);
    expect(class3).toBe(class1); // 第三次应该回到初始状态
  });

  test('应该在整个页面中应用主题变化', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // 获取页面主要元素的初始颜色
    const getColors = () => page.evaluate(() => {
      const body = getComputedStyle(document.body);
      const heading = getComputedStyle(document.querySelector('h1')!);
      return {
        bodyBg: body.backgroundColor,
        bodyColor: body.color,
        headingColor: heading.color,
      };
    });

    const initialColors = await getColors();
    console.log('Initial colors:', initialColors);

    // 切换主题
    const toggleButton = page.locator('button[aria-label="切换主题"]');
    await toggleButton.click();
    await page.waitForTimeout(100);

    const newColors = await getColors();
    console.log('New colors:', newColors);

    // 验证所有主要元素的颜色都发生了变化
    expect(newColors.bodyBg).not.toBe(initialColors.bodyBg);
    expect(newColors.bodyColor).not.toBe(initialColors.bodyColor);
    expect(newColors.headingColor).not.toBe(initialColors.headingColor);
  });
});
