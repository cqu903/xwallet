'use client';

import { useTheme } from 'next-themes';
import { Moon, Sun } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Button } from './button';

/**
 * 主题切换按钮组件
 *
 * 功能：
 * - 在亮色/暗色模式之间切换
 * - 使用 mounted 状态防止 hydration 不匹配
 * - 使用 resolvedTheme 作为 fallback
 * - 支持无障碍访问（screen reader）
 */
export function ThemeToggle() {
  const { theme, setTheme, resolvedTheme } = useTheme();
  const [mounted, setMounted] = useState(false);

  // 等待客户端挂载完成，避免 hydration 不匹配
  useEffect(() => {
    setMounted(true);
  }, []);

  // 使用 resolvedTheme 或 theme，并处理 undefined 的情况
  // 在客户端挂载前使用 'light' 作为默认值
  const currentTheme = mounted ? (theme || resolvedTheme || 'light') : 'light';

  const handleThemeToggle = () => {
    // 切换到相反的主题
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    setTheme(newTheme);
  };

  return (
    <Button
      variant="ghost"
      size="icon"
      onClick={handleThemeToggle}
      disabled={!mounted}
      aria-label="切换主题"
    >
      <Sun className="h-5 w-5 rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
      <Moon className="absolute h-5 w-5 rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
      <span className="sr-only">切换主题</span>
    </Button>
  );
}
