import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LoginForm } from '../LoginForm';
import { useAuthStore } from '@/lib/stores';
import { NextIntlClientProvider } from 'next-intl';
import { useRouter } from 'next/navigation';
import { login } from '@/lib/api/auth';

// Mock dependencies
jest.mock('@/lib/stores', () => ({
  useAuthStore: jest.fn(),
}));

jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
  useParams: jest.fn(() => ({ locale: 'zh-CN' })),
}));

jest.mock('@/lib/api/auth', () => ({
  login: jest.fn(),
}));

const mockPush = jest.fn();
const mockReplace = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
  (useRouter as jest.Mock).mockReturnValue({
    replace: mockReplace,
    push: mockPush,
  });
});

// Test translations
const translations = {
  auth: {
    loginTitle: '登录',
    loginSubtitle: '请使用您的工号登录',
    employeeNo: '工号',
    employeeNoPlaceholder: '请输入工号',
    password: '密码',
    passwordPlaceholder: '请输入密码',
    rememberMe: '记住我',
    login: '登录',
    pleaseInputEmployeeNo: '请输入工号',
    pleaseInputPassword: '请输入密码',
    loginFailed: '登录失败',
    loginFailedWithRetry: '登录失败，还有 {count} 次尝试机会',
    accountLocked: '账户已锁定，请 {seconds} 秒后重试',
    locked: '已锁定 {seconds} 秒',
  },
  common: {
    loading: '加载中...',
  },
};

function renderWithTranslations(component: React.ReactElement) {
  return render(
    <NextIntlClientProvider locale="zh-CN" messages={translations}>
      {component}
    </NextIntlClientProvider>
  );
}

describe('LoginForm', () => {
  describe('Hydration consistency', () => {
    it('should return null when authenticated on server and client', () => {
      // Simulate authenticated state
      (useAuthStore as jest.Mock).mockReturnValue({
        isAuthenticated: true,
      });

      const { container } = renderWithTranslations(<LoginForm />);

      // Component should render null (empty container)
      expect(container.firstChild).toBe(null);
    });

    it('should render form consistently when not authenticated', () => {
      // Simulate unauthenticated state
      (useAuthStore as jest.Mock).mockReturnValue({
        isAuthenticated: false,
      });

      const { container } = renderWithTranslations(<LoginForm />);

      // Should render form element
      expect(container.querySelector('form')).toBeInTheDocument();
      expect(screen.getByLabelText('工号')).toBeInTheDocument();
      expect(screen.getByLabelText('密码')).toBeInTheDocument();
    });

    it('should not change render output during hydration', async () => {
      // Start with unauthenticated state
      (useAuthStore as jest.Mock).mockReturnValue({
        isAuthenticated: false,
      });

      const { container } = renderWithTranslations(<LoginForm />);

      // Verify initial render
      const form = container.querySelector('form');
      expect(form).toBeInTheDocument();

      // Simulate state change after mount (not during hydration)
      (useAuthStore as jest.Mock).mockReturnValue({
        isAuthenticated: true,
      });

      // Re-render to simulate state change
      const { container: newContainer } = renderWithTranslations(<LoginForm />);

      // After state change, should render null
      expect(newContainer.firstChild).toBe(null);
    });
  });

  describe('Form validation', () => {
    it('should show error when submitting empty employee number', async () => {
      (useAuthStore as jest.Mock).mockReturnValue({
        isAuthenticated: false,
      });

      renderWithTranslations(<LoginForm />);

      const submitButton = screen.getByRole('button', { name: '登录' });
      await userEvent.click(submitButton);

      expect(screen.getByText('请输入工号')).toBeInTheDocument();
    });

    it('should show error when submitting empty password', async () => {
      (useAuthStore as jest.Mock).mockReturnValue({
        isAuthenticated: false,
      });

      renderWithTranslations(<LoginForm />);

      const employeeNoInput = screen.getByLabelText('工号');
      await userEvent.type(employeeNoInput, 'ADMIN001');

      const submitButton = screen.getByRole('button', { name: '登录' });
      await userEvent.click(submitButton);

      expect(screen.getByText('请输入密码')).toBeInTheDocument();
    });
  });

  describe('Login flow', () => {
    it('should call login API and redirect on success', async () => {
      (useAuthStore as jest.Mock).mockReturnValue({
        isAuthenticated: false,
      });
      (login as jest.Mock).mockResolvedValue(undefined);

      renderWithTranslations(<LoginForm />);

      const employeeNoInput = screen.getByLabelText('工号');
      const passwordInput = screen.getByLabelText('密码');

      await userEvent.type(employeeNoInput, 'ADMIN001');
      await userEvent.type(passwordInput, 'admin123');

      const submitButton = screen.getByRole('button', { name: '登录' });
      await userEvent.click(submitButton);

      await waitFor(() => {
        expect(login).toHaveBeenCalledWith({
          employeeNo: 'ADMIN001',
          password: 'admin123',
          rememberMe: false,
        });
        expect(mockReplace).toHaveBeenCalledWith('/zh-CN/dashboard');
      });
    });

    it('should handle login failure with remaining attempts', async () => {
      (useAuthStore as jest.Mock).mockReturnValue({
        isAuthenticated: false,
      });
      (login as jest.Mock).mockRejectedValue({
        remainingAttempts: 2,
      });

      renderWithTranslations(<LoginForm />);

      const employeeNoInput = screen.getByLabelText('工号');
      const passwordInput = screen.getByLabelText('密码');

      await userEvent.type(employeeNoInput, 'ADMIN001');
      await userEvent.type(passwordInput, 'wrongpass');

      const submitButton = screen.getByRole('button', { name: '登录' });
      await userEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/登录失败，还有 2 次尝试机会/)).toBeInTheDocument();
      });
    });
  });

  describe('Password visibility toggle', () => {
    it('should toggle password visibility', async () => {
      (useAuthStore as jest.Mock).mockReturnValue({
        isAuthenticated: false,
      });

      renderWithTranslations(<LoginForm />);

      const passwordInput = screen.getByLabelText('密码') as HTMLInputElement;
      const toggleButton = passwordInput.nextElementSibling?.querySelector('button');

      expect(passwordInput.type).toBe('password');

      if (toggleButton) {
        await userEvent.click(toggleButton);
        expect(passwordInput.type).toBe('text');

        await userEvent.click(toggleButton);
        expect(passwordInput.type).toBe('password');
      }
    });
  });

  describe('Remember me checkbox', () => {
    it('should toggle remember me state', async () => {
      (useAuthStore as jest.Mock).mockReturnValue({
        isAuthenticated: false,
      });

      renderWithTranslations(<LoginForm />);

      const checkbox = screen.getByRole('checkbox', { name: '记住我' });
      expect(checkbox).not.toBeChecked();

      await userEvent.click(checkbox);
      expect(checkbox).toBeChecked();

      await userEvent.click(checkbox);
      expect(checkbox).not.toBeChecked();
    });
  });
});
