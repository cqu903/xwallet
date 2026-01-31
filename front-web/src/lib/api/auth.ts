import { post } from './client';
import { useAuthStore } from '@/lib/stores';

const REMEMBER_ME_KEY = 'auth-storage';
const REMEMBER_ME_DURATION = 7 * 24 * 60 * 60 * 1000; // 7天（毫秒）

/** 管理后台登录入参：工号 + 密码 + 记住我 */
export interface LoginRequest {
  employeeNo: string;
  password: string;
  rememberMe?: boolean;
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
 * 检查存储的认证信息是否过期
 */
function checkExpiration(): boolean {
  if (typeof window === 'undefined') return false;

  const stored = localStorage.getItem(REMEMBER_ME_KEY);
  if (!stored) return false;

  try {
    const data = JSON.parse(stored);
    if (data.state?._timestamp) {
      const elapsed = Date.now() - data.state._timestamp;
      // 如果超过7天，清除存储并返回true（已过期）
      if (elapsed > REMEMBER_ME_DURATION) {
        localStorage.removeItem(REMEMBER_ME_KEY);
        return true;
      }
    }
    return false;
  } catch {
    return false;
  }
}

/**
 * 登录（管理后台：userType=SYSTEM，account=工号）
 * @param credentials 登录凭证，包含工号、密码和是否记住登录
 */
export async function login(credentials: LoginRequest): Promise<void> {
  // 检查是否过期
  checkExpiration();

  const body = {
    userType: 'SYSTEM',
    account: credentials.employeeNo,
    password: credentials.password,
    rememberMe: credentials.rememberMe || false,
  };
  const raw = await post<ApiResult<LoginResponseData>>('/auth/login', body);

  if (raw.code !== 200 || !raw.data) {
    // 解析错误信息中的剩余尝试次数和锁定时间
    const message = raw?.message || '登录失败';
    const remainingAttemptsMatch = message.match(/剩余尝试次数[：:]\s*(\d+)/);
    const lockoutTimeMatch = message.match(/锁定[：:]\s*(\d+)/);

    const error = new Error(message) as Error & { remainingAttempts?: number; lockoutTime?: number };
    if (remainingAttemptsMatch) {
      error.remainingAttempts = parseInt(remainingAttemptsMatch[1], 10);
    }
    if (lockoutTimeMatch) {
      error.lockoutTime = parseInt(lockoutTimeMatch[1], 10);
    }
    throw error;
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
  // 检查是否过期
  const isExpired = checkExpiration();
  if (isExpired) {
    // 如果过期，触发登出
    useAuthStore.getState().logout();
    return null;
  }

  return useAuthStore.getState().user;
}
