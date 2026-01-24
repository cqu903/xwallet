import { get } from './client';
import { useMenuStore } from '@/lib/stores';

export interface MenuItem {
  id: string;
  name: string;
  path: string;
  children?: MenuItem[];
}

/**
 * 获取用户菜单
 */
export async function fetchMenus(): Promise<MenuItem[]> {
  const response = await get<MenuItem[]>('/menu/list');

  // 保存认证信息到 Zustand store
  useMenuStore.getState().setMenus(response);

  return response;
}

/**
 * 清除菜单缓存
 */
export function clearMenus() {
  useMenuStore.getState().clearMenus();
}
