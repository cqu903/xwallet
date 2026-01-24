import { post } from './client';
import { useAuthStore } from '@/lib/stores';

/** 管理后台登录入参：工号 + 密码 */
export interface LoginRequest {
  employeeNo: string;
  password: string;
}

/** 后端 LoginResponse（data 字段结构） */
interface LoginResponseData {
  token: string;
  userInfo: {
    userId: number;
    username: string;
    userType: string;
    roles?: string[];
  };
}

/** 后端统一包装：ResponseResult<T> */
interface ApiResult<T> {
  code?: number;
  message?: string;
  data?: T;
}

/**
 * 登录（管理后台：userType=SYSTEM，account=工号）
 */
export async function login(credentials: LoginRequest): Promise<void> {
  const body = {
    userType: 'SYSTEM',
    account: credentials.employeeNo,
    password: credentials.password,
  };
  const raw = await post<ApiResult<LoginResponseData>>('/auth/login', body);

  if (raw.code !== 200 || !raw.data) {
    throw new Error(raw?.message || '登录失败');
  }

  const { token, userInfo } = raw.data;

  const user = {
    id: userInfo.userId,
    employeeNo: credentials.employeeNo,
    username: userInfo.username,
    email: '',
    status: 1,
    roles: (userInfo.roles || []).map((r, i) => ({
      id: i,
      roleCode: r,
      roleName: r,
    })),
  };

  useAuthStore.getState().setAuth(user, token, []);
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
