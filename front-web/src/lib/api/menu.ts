import { get } from './client';
import { useMenuStore } from '@/lib/stores';

export interface MenuItem {
  id: string;
  name: string;
  /** 叶子才有 path；目录/父级可为 null */
  path?: string | null;
  children?: MenuItem[];
}

/** 后端统一包装 ResponseResult<T> */
interface ApiResult<T> {
  code?: number;
  message?: string;
  data?: T;
}

/**
 * 从后端获取菜单原始数据（已解析 ResponseResult）
 */
export async function getMenus(): Promise<MenuItem[]> {
  const r = await get<ApiResult<MenuItem[]>>('/menus');
  if (r.code !== 200 || !r.data) {
    throw new Error(r?.message || '获取菜单失败');
  }
  return r.data;
}

/**
 * 获取用户菜单并写入 store
 */
export async function fetchMenus(): Promise<MenuItem[]> {
  const menus = await getMenus();
  useMenuStore.getState().setMenus(menus);
  return menus;
}

/**
 * 清除菜单缓存
 */
export function clearMenus() {
  useMenuStore.getState().clearMenus();
}
