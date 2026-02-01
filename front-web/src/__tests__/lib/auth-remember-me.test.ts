/**
 * TDD 测试: "记住我"功能
 *
 * 预期行为:
 * 1. 勾选"记住我"后，登录状态应持久化到 localStorage，有效期 7 天
 * 2. 不勾选"记住我"后，登录状态不应持久化（仅 session 级别）
 * 3. 过期的持久化状态应被清除，用户需重新登录
 */

import { act, renderHook, waitFor } from '@testing-library/react';
import { useAuthStore } from '@/lib/stores/auth-store';
import { login, logout } from '@/lib/api/auth';
import { post } from '@/lib/api/client';

// Mock dependencies
jest.mock('@/lib/api/client', () => ({
  post: jest.fn(),
}));

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value;
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

describe('Remember Me Functionality (TDD)', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();
    jest.clearAllMocks();
  });

  describe('场景 1: 勾选"记住我"', () => {
    it('应该将认证信息持久化到 localStorage', async () => {
      // Mock API 响应
      (post as jest.Mock).mockResolvedValue({
        code: 200,
        data: {
          token: 'mock-jwt-token',
          userInfo: {
            userId: 1,
            username: 'Admin User',
            userType: 'SYSTEM',
            roles: ['ADMIN'],
          },
        },
      });

      // 执行登录（勾选记住我）
      await act(async () => {
        await login({
          employeeNo: 'ADMIN001',
          password: 'admin123',
          rememberMe: true, // 勾选记住我
        });
      });

      // 验证：认证信息已存储到 localStorage
      const storedAuth = localStorage.getItem('auth-storage');
      expect(storedAuth).not.toBeNull();

      const authData = JSON.parse(storedAuth!);
      expect(authData.user).toBeDefined();
      expect(authData.token).toBe('mock-jwt-token');
      expect(authData.isAuthenticated).toBe(true);
      expect(authData._timestamp).toBeDefined();
    });

    it('应该在 7 天内保持登录状态', async () => {
      (post as jest.Mock).mockResolvedValue({
        code: 200,
        data: {
          token: 'mock-jwt-token',
          userInfo: {
            userId: 1,
            username: 'Admin User',
            userType: 'SYSTEM',
            roles: ['ADMIN'],
          },
        },
      });

      // 登录并记住我
      await act(async () => {
        await login({
          employeeNo: 'ADMIN001',
          password: 'admin123',
          rememberMe: true,
        });
      });

      // 获取存储的时间戳
      const storedAuth = localStorage.getItem('auth-storage');
      const authData = JSON.parse(storedAuth!);
      const timestamp = authData._timestamp;

      // 模拟 6 天后（未过期）
      const sixDaysLater = timestamp + 6 * 24 * 60 * 60 * 1000;
      jest.spyOn(Date, 'now').mockReturnValue(sixDaysLater);

      // 重新创建 store（模拟页面刷新）
      const { result } = renderHook(() => useAuthStore());

      // 验证：用户仍然处于登录状态
      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
        expect(result.current.user).toBeDefined();
      });
    });

    it('应该在 7 天后过期并清除认证信息', async () => {
      (post as jest.Mock).mockResolvedValue({
        code: 200,
        data: {
          token: 'mock-jwt-token',
          userInfo: {
            userId: 1,
            username: 'Admin User',
            userType: 'SYSTEM',
            roles: ['ADMIN'],
          },
        },
      });

      // 登录并记住我
      await act(async () => {
        await login({
          employeeNo: 'ADMIN001',
          password: 'admin123',
          rememberMe: true,
        });
      });

      // 获取存储的时间戳
      const storedAuth = localStorage.getItem('auth-storage');
      const authData = JSON.parse(storedAuth!);
      const timestamp = authData._timestamp;

      // 模拟 8 天后（已过期）
      const eightDaysLater = timestamp + 8 * 24 * 60 * 60 * 1000;
      jest.spyOn(Date, 'now').mockReturnValue(eightDaysLater);

      // 调用 refresh 方法（模拟 store 重新检查 localStorage）
      const { result } = renderHook(() => useAuthStore());
      await act(async () => {
        result.current.refresh();
      });

      // 验证：localStorage 已被清除
      expect(localStorage.getItem('auth-storage')).toBeNull();

      // 验证：用户已登出
      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(false);
        expect(result.current.user).toBeNull();
      });
    });
  });

  describe('场景 2: 不勾选"记住我"', () => {
    it('不应该将认证信息持久化到 localStorage', async () => {
      (post as jest.Mock).mockResolvedValue({
        code: 200,
        data: {
          token: 'mock-jwt-token',
          userInfo: {
            userId: 1,
            username: 'Admin User',
            userType: 'SYSTEM',
            roles: ['ADMIN'],
          },
        },
      });

      // 执行登录（不勾选记住我）
      await act(async () => {
        await login({
          employeeNo: 'ADMIN001',
          password: 'admin123',
          rememberMe: false, // 不勾选记住我
        });
      });

      // 验证：认证信息未存储到 localStorage
      const storedAuth = localStorage.getItem('auth-storage');
      expect(storedAuth).toBeNull();
    });

    it('应该在当前会话中保持登录状态', async () => {
      (post as jest.Mock).mockResolvedValue({
        code: 200,
        data: {
          token: 'mock-jwt-token',
          userInfo: {
            userId: 1,
            username: 'Admin User',
            userType: 'SYSTEM',
            roles: ['ADMIN'],
          },
        },
      });

      // 登录（不勾选记住我）
      await act(async () => {
        await login({
          employeeNo: 'ADMIN001',
          password: 'admin123',
          rememberMe: false,
        });
      });

      const { result } = renderHook(() => useAuthStore());

      // 验证：当前会话中用户已登录
      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
        expect(result.current.user).toBeDefined();
      });
    });
  });

  describe('场景 3: 主动登出', () => {
    it('登出时应清除 localStorage 中的认证信息', async () => {
      (post as jest.Mock).mockResolvedValue({
        code: 200,
        data: {
          token: 'mock-jwt-token',
          userInfo: {
            userId: 1,
            username: 'Admin User',
            userType: 'SYSTEM',
            roles: ['ADMIN'],
          },
        },
      });

      // 登录并记住我
      await act(async () => {
        await login({
          employeeNo: 'ADMIN001',
          password: 'admin123',
          rememberMe: true,
        });
      });

      // 验证已存储
      expect(localStorage.getItem('auth-storage')).not.toBeNull();

      // 执行登出
      act(() => {
        logout();
      });

      // 验证：localStorage 已清除
      expect(localStorage.getItem('auth-storage')).toBeNull();

      const { result } = renderHook(() => useAuthStore());
      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(false);
        expect(result.current.user).toBeNull();
      });
    });
  });

  describe('场景 4: 过期检查边界情况', () => {
    it('应该正确处理刚好 7 天的边界情况（仍然有效）', async () => {
      (post as jest.Mock).mockResolvedValue({
        code: 200,
        data: {
          token: 'mock-jwt-token',
          userInfo: {
            userId: 1,
            username: 'Admin User',
            userType: 'SYSTEM',
            roles: ['ADMIN'],
          },
        },
      });

      // 登录
      await act(async () => {
        await login({
          employeeNo: 'ADMIN001',
          password: 'admin123',
          rememberMe: true,
        });
      });

      const storedAuth = localStorage.getItem('auth-storage');
      const authData = JSON.parse(storedAuth!);
      const timestamp = authData._timestamp;

      // 模拟刚好 7 天（边界情况）
      const exactlySevenDays = timestamp + 7 * 24 * 60 * 60 * 1000;
      jest.spyOn(Date, 'now').mockReturnValue(exactlySevenDays);

      // 调用 refresh 方法
      const { result } = renderHook(() => useAuthStore());
      await act(async () => {
        result.current.refresh();
      });

      // 验证：刚好 7 天时仍然有效（只有超过 7 天才过期）
      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });
    });

    it('应该正确处理 7 天减 1 毫秒的边界情况', async () => {
      (post as jest.Mock).mockResolvedValue({
        code: 200,
        data: {
          token: 'mock-jwt-token',
          userInfo: {
            userId: 1,
            username: 'Admin User',
            userType: 'SYSTEM',
            roles: ['ADMIN'],
          },
        },
      });

      // 登录
      await act(async () => {
        await login({
          employeeNo: 'ADMIN001',
          password: 'admin123',
          rememberMe: true,
        });
      });

      const storedAuth = localStorage.getItem('auth-storage');
      const authData = JSON.parse(storedAuth!);
      const timestamp = authData._timestamp;

      // 模拟 7 天减 1 毫秒（未过期）
      const justBeforeSevenDays = timestamp + 7 * 24 * 60 * 60 * 1000 - 1;
      jest.spyOn(Date, 'now').mockReturnValue(justBeforeSevenDays);

      // 验证：应该仍然有效
      const { result } = renderHook(() => useAuthStore());
      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });
    });
  });
});
