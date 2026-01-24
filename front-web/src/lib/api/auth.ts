import { post } from './client';
import { useAuthStore } from '@/lib/stores';

export interface LoginRequest {
  employeeNo: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
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
  };
  permissions: string[];
}

/**
 * 登录
 */
export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  const response = await post<LoginResponse>('/auth/login', credentials);

  // 保存认证信息到 Zustand store
  useAuthStore.getState().setAuth(
    response.user,
    response.token,
    response.permissions
  );

  return response;
}

/**
 * 登出
 */
export function logout() {
  useAuthStore.getState().logout();
}

/**
 * 获取当前用户信息
 */
export async function getCurrentUser() {
  return useAuthStore.getState().user;
}
