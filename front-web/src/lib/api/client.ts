import { useAuthStore } from '@/lib/stores';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

/**
 * Fetch API 封装
 * 自动注入 JWT Token 和处理错误
 */
export async function fetchApi<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = useAuthStore.getState().token;

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const url = `${API_BASE_URL}${endpoint}`;

  try {
    const response = await fetch(url, { ...options, headers });

    if (response.status === 401) {
      useAuthStore.getState().logout();
      if (typeof window !== 'undefined') {
        const m = /^\/([a-z]{2}-[A-Z]{2})\b/.exec(window.location.pathname);
        const locale = m ? m[1] : 'zh-CN';
        window.location.href = `/${locale}/login`;
      }
      throw new Error('Unauthorized');
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(errorData.message || '请求失败');
    }

    return await response.json();
  } catch (error) {
    if (error instanceof Error) throw error;
    throw new Error('网络错误');
  }
}

/**
 * GET 请求
 */
export function get<T>(endpoint: string, options?: RequestInit): Promise<T> {
  return fetchApi<T>(endpoint, { method: 'GET', ...options });
}

/**
 * POST 请求
 */
export function post<T>(endpoint: string, data?: unknown): Promise<T> {
  return fetchApi<T>(endpoint, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/**
 * PUT 请求
 */
export function put<T>(endpoint: string, data?: unknown, options?: RequestInit): Promise<T> {
  return fetchApi<T>(endpoint, {
    method: 'PUT',
    body: JSON.stringify(data),
    ...options,
  });
}

/**
 * DELETE 请求
 */
export function del<T>(endpoint: string): Promise<T> {
  return fetchApi<T>(endpoint, { method: 'DELETE' });
}
