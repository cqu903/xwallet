import { create } from 'zustand';
import React from 'react';

const REMEMBER_ME_DURATION = 7 * 24 * 60 * 60 * 1000; // 7天（毫秒）
const STORAGE_KEY = 'auth-storage';
const SESSION_STORAGE_KEY = 'auth-session';

/**
 * 检查存储的认证信息是否过期
 */
function checkExpiration(storageKey: string, storage: Storage): boolean {
  if (typeof window === 'undefined') return false;

  const stored = storage.getItem(storageKey);
  if (!stored) return false;

  try {
    const data = JSON.parse(stored);
    if (data._timestamp) {
      const elapsed = Date.now() - data._timestamp;
      // 只有 localStorage 检查过期时间，sessionStorage 不检查
      if (storage === localStorage && elapsed > REMEMBER_ME_DURATION) {
        storage.removeItem(storageKey);
        return true;
      }
    }
    return false;
  } catch {
    return false;
  }
}

/**
 * 从存储加载认证信息（优先 localStorage，其次 sessionStorage）
 */
function loadFromStorage(): {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
} | null {
  if (typeof window === 'undefined') return null;

  // 优先检查 localStorage（记住我）
  const isLocalExpired = checkExpiration(STORAGE_KEY, localStorage);
  if (!isLocalExpired) {
    const localStored = localStorage.getItem(STORAGE_KEY);
    if (localStored) {
      try {
        const data = JSON.parse(localStored);
        return {
          user: data.user,
          token: data.token,
          isAuthenticated: data.isAuthenticated,
        };
      } catch {
        // 继续检查 sessionStorage
      }
    }
  }

  // 其次检查 sessionStorage（当前会话）
  const sessionStored = sessionStorage.getItem(SESSION_STORAGE_KEY);
  if (sessionStored) {
    try {
      const data = JSON.parse(sessionStored);
      return {
        user: data.user,
        token: data.token,
        isAuthenticated: data.isAuthenticated,
      };
    } catch {
      return null;
    }
  }

  return null;
}

/**
 * 同步初始化认证状态（仅在客户端）
 * 在 store 创建时立即执行，确保首次渲染就有正确的状态
 */
function initializeAuthState() {
  if (typeof window === 'undefined') {
    // SSR 时返回默认未登录状态
    return {
      user: null,
      token: null,
      permissions: [],
      isAuthenticated: false,
    };
  }

  // 客户端：立即从 localStorage 恢复状态（同步，无延迟）
  const stored = loadFromStorage();
  if (stored?.isAuthenticated && stored.user) {
    return {
      user: stored.user,
      token: stored.token,
      permissions: [],
      isAuthenticated: stored.isAuthenticated,
    };
  }

  // 无有效存储时返回未登录状态
  return {
    user: null,
    token: null,
    permissions: [],
    isAuthenticated: false,
  };
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
 * 保存认证信息到存储
 * @param shouldPersist true=localStorage（记住我），false=sessionStorage（当前会话）
 */
function saveToStorage(
  user: User,
  token: string,
  isAuthenticated: boolean,
  shouldPersist: boolean = false
): void {
  if (typeof window === 'undefined') return;

  const data = {
    user,
    token,
    isAuthenticated,
    _timestamp: Date.now(),
  };

  if (shouldPersist) {
    // 保存到 localStorage（记住我，关闭浏览器后仍然有效）
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  } else {
    // 保存到 sessionStorage（当前会话，关闭浏览器后清除）
    sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(data));
  }
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
    // Initial state - 在 store 创建时立即同步初始化
    // 客户端：直接从 localStorage 恢复（如果有）
    // SSR：返回未登录状态
    ...initializeAuthState(),

    // Actions
    setAuth: (user, token, permissions, shouldPersist = false) => {
      // 标记已 hydration
      markHydrated();

      // 根据 shouldPersist 决定保存到哪个存储
      saveToStorage(user, token, true, shouldPersist);

      set({
        user,
        token,
        permissions,
        isAuthenticated: true,
      });
    },

    logout: () => {
      // 清除两个存储
      localStorage.removeItem(STORAGE_KEY);
      sessionStorage.removeItem(SESSION_STORAGE_KEY);

      set({
        user: null,
        token: null,
        permissions: [],
        isAuthenticated: false,
      });
    },

    updateUser: (user) =>
      set((state) => {
        // 如果有 token 和 isAuthenticated，说明已登录且可能需要更新存储
        if (state.token && state.isAuthenticated) {
          // 检查哪个存储有数据
          const localStored = localStorage.getItem(STORAGE_KEY);
          const sessionStored = sessionStorage.getItem(SESSION_STORAGE_KEY);

          if (localStored) {
            // 更新 localStorage 中的用户信息
            saveToStorage(user, state.token, state.isAuthenticated, true);
          } else if (sessionStored) {
            // 更新 sessionStorage 中的用户信息
            saveToStorage(user, state.token, state.isAuthenticated, false);
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
