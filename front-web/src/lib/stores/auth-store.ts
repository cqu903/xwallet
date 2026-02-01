import { create } from 'zustand';
import React from 'react';

const REMEMBER_ME_DURATION = 7 * 24 * 60 * 60 * 1000; // 7天（毫秒）
const STORAGE_KEY = 'auth-storage';

/**
 * 检查存储的认证信息是否过期
 */
function checkExpiration(): boolean {
  if (typeof window === 'undefined') return false;

  const stored = localStorage.getItem(STORAGE_KEY);
  if (!stored) return false;

  try {
    const data = JSON.parse(stored);
    if (data._timestamp) {
      const elapsed = Date.now() - data._timestamp;
      if (elapsed > REMEMBER_ME_DURATION) {
        localStorage.removeItem(STORAGE_KEY);
        return true;
      }
    }
    return false;
  } catch {
    return false;
  }
}

/**
 * 从 localStorage 加载认证信息
 */
function loadFromStorage(): {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
} | null {
  if (typeof window === 'undefined') return null;

  // 检查是否过期
  const isExpired = checkExpiration();
  if (isExpired) {
    return null;
  }

  const stored = localStorage.getItem(STORAGE_KEY);
  if (!stored) return null;

  try {
    const data = JSON.parse(stored);
    return {
      user: data.user,
      token: data.token,
      isAuthenticated: data.isAuthenticated,
    };
  } catch {
    return null;
  }
}

/**
 * 标记是否已在客户端 hydration
 * 用于防止 SSR/CSR 状态不一致导致 hydration 错误
 */
let hasHydrated = false;

/**
 * 标记 hydration 完成，用于在客户端首次渲染时恢复状态
 */
export function markHydrated() {
  if (typeof window !== 'undefined' && !hasHydrated) {
    hasHydrated = true;
  }
}

/**
 * 保存认证信息到 localStorage
 */
function saveToStorage(
  user: User,
  token: string,
  isAuthenticated: boolean
): void {
  if (typeof window === 'undefined') return;

  const data = {
    user,
    token,
    isAuthenticated,
    _timestamp: Date.now(),
  };

  localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
}

/**
 * 导出过期检查函数供测试使用
 */
export { checkExpiration };

/**
 * 刷新认证状态（从 localStorage 重新加载并检查过期）
 * 用于页面刷新或需要重新检查过期时
 */
export function refreshAuth() {
  const stored = loadFromStorage();
  return {
    user: stored?.user || null,
    token: stored?.token || null,
    isAuthenticated: stored?.isAuthenticated || false,
  };
}

/**
 * Hook 用于在客户端首次渲染时触发 auth store hydration
 * 必须在 Client Component 顶部调用
 *
 * 原理：SSR 时 isAuthenticated=false，客户端首次渲染后通过 useEffect
 * 从 localStorage 恢复登录状态，然后触发重新渲染
 */
export function useAuthHydration() {
  const { isAuthenticated, user } = useAuthStore();

  React.useEffect(() => {
    // 只在客户端执行一次
    const stored = loadFromStorage();
    if (stored?.isAuthenticated && stored.user && !isAuthenticated) {
      // 从 localStorage 恢复状态
      useAuthStore.setState({
        user: stored.user,
        token: stored.token,
        isAuthenticated: stored.isAuthenticated,
      });
    }
  }, []); // 空依赖数组，只在挂载时执行一次
}

interface User {
  id: number;
  employeeNo: string;
  username: string;
  email: string;
  status: number;
  roles: Array<{
    id: number;
    roleCode: string;
    roleName: string;
  }>;
}

interface AuthState {
  // State
  user: User | null;
  token: string | null;
  permissions: string[];
  isAuthenticated: boolean;

  // Actions
  setAuth: (user: User, token: string, permissions: string[], shouldPersist?: boolean) => void;
  logout: () => void;
  updateUser: (user: User) => void;
  refresh: () => void; // 刷新状态（从 localStorage 重新加载）
}

export const useAuthStore = create<AuthState>()((set, get) => {
  return {
    // Initial state - SSR 和客户端首次渲染都为未登录
    // 真实状态在客户端通过 useAuthHydration hook 恢复
    user: null,
    token: null,
    permissions: [],
    isAuthenticated: false,

    // Actions
    setAuth: (user, token, permissions, shouldPersist = false) => {
      // 标记已 hydration
      markHydrated();

      // 如果需要持久化，保存到 localStorage
      if (shouldPersist) {
        saveToStorage(user, token, true);
      } else {
        // 不需要持久化，清除已有的 localStorage
        localStorage.removeItem(STORAGE_KEY);
      }

      set({
        user,
        token,
        permissions,
        isAuthenticated: true,
      });
    },

    logout: () => {
      // 清除 localStorage
      localStorage.removeItem(STORAGE_KEY);

      set({
        user: null,
        token: null,
        permissions: [],
        isAuthenticated: false,
      });
    },

    updateUser: (user) =>
      set((state) => {
        // 如果有 token 和 isAuthenticated，说明已登录且可能需要更新 localStorage
        if (state.token && state.isAuthenticated) {
          const stored = localStorage.getItem(STORAGE_KEY);
          if (stored) {
            // 更新 localStorage 中的用户信息
            saveToStorage(user, state.token, state.isAuthenticated);
          }
        }

        return {
          ...state,
          user,
        };
      }),

    refresh: () => {
      const refreshed = refreshAuth();
      set(refreshed);
    },
  };
});
