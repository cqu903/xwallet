import useSWR, { SWRConfiguration } from 'swr';
import { get } from '@/lib/api/client';

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
 * 菜单 Hook
 */
export function useMenus() {
  return useApi<MenuItem[]>('/menu/list');
}

import type { MenuItem } from '@/lib/api/menu';
