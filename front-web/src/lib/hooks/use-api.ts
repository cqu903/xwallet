import useSWR, { SWRConfiguration } from 'swr';
import { get } from '@/lib/api/client';
import { fetchMenus } from '@/lib/api/menu';
import type { MenuItem } from '@/lib/api/menu';

/**
 * SWR 配置
 */
const swrConfig: SWRConfiguration = {
  revalidateOnFocus: false,
  shouldRetryOnError: false,
};

/**
 * 通用 SWR Hook
 */
export function useApi<T>(url: string | null, config?: SWRConfiguration) {
  const { data, error, mutate, isLoading } = useSWR<T>(
    url,
    (url) => get<T>(url),
    { ...swrConfig, ...config }
  );

  return {
    data,
    error,
    mutate,
    isLoading,
  };
}

/**
 * 菜单 Hook（/menus 返回 ResponseResult，由 fetchMenus 解包并写入 store）
 */
export function useMenus() {
  return useSWR<MenuItem[]>('menus', fetchMenus, swrConfig);
}
