import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { Header } from '../Header';
import { useTheme } from 'next-themes';
import { useAuthStore } from '@/lib/stores';

// Mock dependencies
jest.mock('next-themes', () => ({
  useTheme: jest.fn(),
}));

jest.mock('next-intl', () => ({
  useTranslations: () => (key: string) => key,
}));

jest.mock('next/navigation', () => ({
  useRouter: () => ({
    push: jest.fn(),
  }),
  usePathname: () => '/zh-CN/dashboard',
}));

jest.mock('@/lib/api/auth', () => ({
  logout: jest.fn(),
}));

jest.mock('@/lib/stores', () => ({
  useAuthStore: () => ({
    user: { username: 'TestUser' },
  }),
}));

describe('Header - Theme Toggle', () => {
  const mockSetTheme = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useTheme as jest.Mock).mockReturnValue({
      theme: 'light',
      setTheme: mockSetTheme,
      resolvedTheme: 'light',
    });
  });

  it('应该渲染主题切换按钮', () => {
    render(<Header />);
    const themeButton = screen.getAllByRole('button')[1]; // 第二个按钮是主题切换按钮
    expect(themeButton).toBeInTheDocument();
  });

  it('应该从亮色模式切换到暗色模式', async () => {
    (useTheme as jest.Mock).mockReturnValue({
      theme: 'light',
      setTheme: mockSetTheme,
      resolvedTheme: 'light',
    });

    render(<Header />);
    const themeButton = screen.getAllByRole('button')[1];

    // 等待组件挂载完成
    await waitFor(() => {
      expect(themeButton).not.toBeDisabled();
    });

    fireEvent.click(themeButton);
    expect(mockSetTheme).toHaveBeenCalledWith('dark');
  });

  it('应该从暗色模式切换到亮色模式', async () => {
    (useTheme as jest.Mock).mockReturnValue({
      theme: 'dark',
      setTheme: mockSetTheme,
      resolvedTheme: 'dark',
    });

    render(<Header />);
    const themeButton = screen.getAllByRole('button')[1];

    await waitFor(() => {
      expect(themeButton).not.toBeDisabled();
    });

    fireEvent.click(themeButton);
    expect(mockSetTheme).toHaveBeenCalledWith('light');
  });

  it('应该正确处理 undefined theme（使用 resolvedTheme）', async () => {
    (useTheme as jest.Mock).mockReturnValue({
      theme: undefined,
      setTheme: mockSetTheme,
      resolvedTheme: 'dark',
    });

    render(<Header />);
    const themeButton = screen.getAllByRole('button')[1];

    await waitFor(() => {
      expect(themeButton).not.toBeDisabled();
    });

    fireEvent.click(themeButton);
    // 当 theme 是 undefined 但 resolvedTheme 是 'dark' 时，应该切换到 'light'
    expect(mockSetTheme).toHaveBeenCalledWith('light');
  });

  it('应该在挂载后启用主题按钮', async () => {
    render(<Header />);
    const themeButton = screen.getAllByRole('button')[1];

    // 等待 useEffect 执行完成
    await waitFor(() => {
      expect(themeButton).not.toBeDisabled();
    });
  });

  it('应该显示太阳图标在亮色模式下', () => {
    (useTheme as jest.Mock).mockReturnValue({
      theme: 'light',
      setTheme: mockSetTheme,
      resolvedTheme: 'light',
    });

    const { container } = render(<Header />);
    const sunIcon = container.querySelector('.rotate-0');
    expect(sunIcon).toBeInTheDocument();
  });

  it('应该显示月亮图标在暗色模式下', () => {
    (useTheme as jest.Mock).mockReturnValue({
      theme: 'dark',
      setTheme: mockSetTheme,
      resolvedTheme: 'dark',
    });

    const { container } = render(<Header />);
    const moonIcon = container.querySelector('.rotate-90');
    expect(moonIcon).toBeInTheDocument();
  });
});
