import { create } from 'zustand';
import { persist } from 'zustand/middleware';

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
  setAuth: (user: User, token: string, permissions: string[]) => void;
  logout: () => void;
  updateUser: (user: User) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      // Initial state
      user: null,
      token: null,
      permissions: [],
      isAuthenticated: false,

      // Actions
      setAuth: (user, token, permissions) =>
        set({
          user,
          token,
          permissions,
          isAuthenticated: true,
        }),

      logout: () =>
        set({
          user: null,
          token: null,
          permissions: [],
          isAuthenticated: false,
        }),

      updateUser: (user) =>
        set((state) => ({
          ...state,
          user,
        })),
    }),
    {
      name: 'auth-storage',
      // Only persist user and token, not permissions (they will be fetched from API)
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
        _timestamp: Date.now(), // 添加时间戳用于过期检查
      }),
    }
  )
);
