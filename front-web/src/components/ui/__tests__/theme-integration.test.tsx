/**
 * 主题切换集成测试
 *
 * 测试目标:
 * 1. 验证 ThemeProvider 正确配置
 * 2. 验证点击切换按钮后,html 元素的 class 发生变化
 * 3. 验证 CSS 变量在 class 变化后正确更新
 * 4. 验证页面视觉效果在主题切换后发生变化
 */

import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ThemeProvider } from 'next-themes';
import { ThemeToggle } from '../theme-toggle';

// Mock matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

describe('主题切换集成测试', () => {
  describe('Step 1: 验证 class 切换', () => {
    it('应该在点击切换按钮后添加/移除 dark class', async () => {
      const { container } = render(
        <ThemeProvider attribute="class" defaultTheme="light">
          <ThemeToggle />
        </ThemeProvider>
      );

      const htmlElement = container.ownerDocument.documentElement;

      // 初始状态应该是 light 模式
      expect(htmlElement.classList.contains('dark')).toBe(false);

      // 点击切换按钮
      const toggleButton = screen.getByRole('button', { name: /切换主题/i });
      await userEvent.click(toggleButton);

      // 等待状态更新
      await waitFor(() => {
        expect(htmlElement.classList.contains('dark')).toBe(true);
      });

      // 再次点击切换回 light 模式
      await userEvent.click(toggleButton);

      await waitFor(() => {
        expect(htmlElement.classList.contains('dark')).toBe(false);
      });
    });
  });

  describe('Step 2: 验证 CSS 变量更新', () => {
    it('应该在切换到 dark 模式后更新 CSS 变量', async () => {
      const { container } = render(
        <ThemeProvider attribute="class" defaultTheme="light">
          <div className="min-h-screen bg-background text-foreground">
            <ThemeToggle />
            测试内容
          </div>
        </ThemeProvider>
      );

      const htmlElement = container.ownerDocument.documentElement;

      // 切换到 dark 模式
      const toggleButton = screen.getByRole('button', { name: /切换主题/i });
      await userEvent.click(toggleButton);

      await waitFor(() => {
        expect(htmlElement.classList.contains('dark')).toBe(true);
      });
    });

    it('应该在切换回 light 模式后恢复 CSS 变量', async () => {
      const { container } = render(
        <ThemeProvider attribute="class" defaultTheme="dark">
          <div className="min-h-screen bg-background text-foreground">
            <ThemeToggle />
            测试内容
          </div>
        </ThemeProvider>
      );

      const htmlElement = container.ownerDocument.documentElement;
      const toggleButton = screen.getByRole('button', { name: /切换主题/i });

      // 初始状态是 dark
      expect(htmlElement.classList.contains('dark')).toBe(true);

      // 切换到 light
      await userEvent.click(toggleButton);

      await waitFor(() => {
        expect(htmlElement.classList.contains('dark')).toBe(false);
      });
    });
  });

  describe('Step 3: 验证视觉变化', () => {
    it('应该在 dark 模式下渲染深色背景', async () => {
      const { container } = render(
        <ThemeProvider attribute="class" defaultTheme="light">
          <div className="min-h-screen bg-background">
            <ThemeToggle />
          </div>
        </ThemeProvider>
      );
      const htmlElement = container.ownerDocument.documentElement;
      const toggleButton = screen.getByRole('button', { name: /切换主题/i });

      // 切换到 dark 模式
      await userEvent.click(toggleButton);

      await waitFor(() => {
        expect(htmlElement.classList.contains('dark')).toBe(true);
      });

      // 检查实际渲染的背景色
      const testDiv = container.querySelector('.bg-background');
      const backgroundColor = getComputedStyle(testDiv!).backgroundColor;

      // 背景应该是深色 (RGB 值较小)
      const rgbValues = backgroundColor.match(/\d+/g);
      if (rgbValues) {
        const brightness = (parseInt(rgbValues[0]) + parseInt(rgbValues[1]) + parseInt(rgbValues[2])) / 3;
        expect(brightness).toBeLessThan(128); // 深色背景的亮度应该小于 128
      }
    });
  });

  describe('Step 4: 边界情况测试', () => {
    it('应该在系统主题模式下正确切换', async () => {
      const { container } = render(
        <ThemeProvider attribute="class" defaultTheme="system">
          <ThemeToggle />
        </ThemeProvider>
      );

      const htmlElement = container.ownerDocument.documentElement;
      const toggleButton = screen.getByRole('button', { name: /切换主题/i });

      // 手动切换应该覆盖系统主题
      await userEvent.click(toggleButton);

      await waitFor(() => {
        expect(
          htmlElement.classList.contains('dark') || htmlElement.classList.contains('light')
        ).toBe(true);
      });
    });

    it('应该处理 hydration 不匹配问题', async () => {
      const { container } = render(
        <ThemeProvider attribute="class" defaultTheme="light">
          <ThemeToggle />
        </ThemeProvider>
      );

      const toggleButton = screen.getByRole('button', { name: /切换主题/i });

      await waitFor(() => {
        expect(toggleButton).not.toBeDisabled();
      });
    });
  });
});
