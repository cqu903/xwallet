import { render } from '@testing-library/react';
import DashboardPage from '../page';

// Mock dependencies
jest.mock('next-intl', () => ({
  useTranslations: () => (key: string) => {
    const translations: Record<string, string> = {
      'dashboard.totalUsers': '总用户数',
      'dashboard.totalRoles': '角色总数',
      'dashboard.todayLogs': '今日日志',
      'dashboard.recentActivities': '最近活动',
    };
    return translations[key] || key;
  },
}));

jest.mock('@/lib/stores', () => ({
  useAuthStore: () => ({
    user: {
      username: '系统管理员',
      employeeNo: 'ADMIN001',
    },
  }),
}));

describe('Welcome Banner Contrast', () => {
  beforeEach(() => {
    // 确保在 light 模式下测试
    document.documentElement.classList.remove('dark');
  });

  it('should have proper background classes for light mode', () => {
    const { container } = render(<DashboardPage />);

    // 获取欢迎横幅
    const banner = container.querySelector('.rounded-2xl.border');
    expect(banner).toBeInTheDocument();

    // 验证在 light 模式下使用浅色背景
    expect(banner).toHaveClass('bg-gradient-to-br');
    expect(banner?.className).toContain('from-primary/10');
    expect(banner?.className).toContain('dark:from-primary');
  });

  it('should use appropriate text colors for light mode', () => {
    const { container } = render(<DashboardPage />);

    // 获取欢迎标题元素
    const welcomeTitle = container.querySelector('h1');
    expect(welcomeTitle).toBeInTheDocument();

    // 在 light 模式下应该使用 text-foreground 而不是 text-white
    expect(welcomeTitle).toHaveClass('text-foreground');
    expect(welcomeTitle?.className).toContain('dark:text-white');
  });

  it('should use proper icon background in light mode', () => {
    const { container } = render(<DashboardPage />);

    // 查找图标容器 (包含 primary/20 的)
    const iconContainer = container.querySelector('.bg-primary\\/20');
    expect(iconContainer).toBeInTheDocument();

    // 验证图标颜色
    expect(iconContainer?.className).toContain('dark:bg-white/20');
  });

  it('should have proper badge styling for light mode', () => {
    const { container } = render(<DashboardPage />);

    // 获取徽章元素
    const badges = container.querySelectorAll('span');
    const loginBadge = Array.from(badges).find(
      (badge) => badge.textContent?.includes('上次登录')
    );

    expect(loginBadge).toBeInTheDocument();

    // 在 light 模式下使用 primary/10 背景和 primary 文字
    expect(loginBadge?.className).toContain('bg-primary/10');
    expect(loginBadge?.className).toContain('dark:bg-white/15');
    expect(loginBadge?.className).toContain('text-primary');
    expect(loginBadge?.className).toContain('dark:text-white');
  });

  it('should maintain readability in dark mode', () => {
    // 切换到 dark 模式
    document.documentElement.classList.add('dark');

    const { container } = render(<DashboardPage />);

    const welcomeTitle = container.querySelector('h1');
    expect(welcomeTitle).toBeInTheDocument();

    // 在 dark 模式下文字应该是白色
    expect(welcomeTitle?.className).toContain('dark:text-white');
  });
});
