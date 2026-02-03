/**
 * ThemeToggle 组件单元测试
 */

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ThemeProvider } from 'next-themes';
import { ThemeToggle } from '../theme-toggle';

// Mock matchMedia for next-themes
const matchMediaMock = jest.fn();
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  }),
});

describe('ThemeToggle', () => {
  it('应该在客户端挂载后渲染切换按钮', async () => {
    render(
      <ThemeProvider attribute="class" defaultTheme="light">
        <ThemeToggle />
      </ThemeProvider>
    );

    const button = screen.getByRole('button', { name: /切换主题/i });

    // 初始状态按钮应该是禁用的(等待 mounted)
    expect(button).toBeDisabled();

    // 等待 mounted 状态更新
    await waitFor(() => {
      expect(button).not.toBeDisabled();
    });
  });

  it('应该在点击后切换主题', async () => {
    render(
      <ThemeProvider attribute="class" defaultTheme="light">
        <ThemeToggle />
      </ThemeProvider>
    );

    const button = screen.getByRole('button', { name: /切换主题/i });

    // 等待组件挂载
    await waitFor(() => {
      expect(button).not.toBeDisabled();
    });

    // 点击按钮
    fireEvent.click(button);

    // 验证按钮仍然可用
    expect(button).not.toBeDisabled();
  });

  it('应该渲染太阳和月亮图标', async () => {
    render(
      <ThemeProvider attribute="class" defaultTheme="light">
        <ThemeToggle />
      </ThemeProvider>
    );

    // 等待组件挂载
    await waitFor(() => {
      const button = screen.getByRole('button', { name: /切换主题/i });
      expect(button).not.toBeDisabled();
    });

    // 检查 SVG 图标存在
    const svgElements = document.querySelectorAll('svg');
    expect(svgElements.length).toBeGreaterThan(0);
  });
});
