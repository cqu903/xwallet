import { get, post, put, del } from './client';
import { useMenuStore } from '@/lib/stores';

/**
 * 菜单类型
 */
export type MenuType = 1 | 2 | 3; // 1-目录 2-菜单 3-按钮

/**
 * 菜单项（用于导航显示）
 */
export interface MenuItem {
  id: string;
  name: string;
  /** 叶子才有 path；目录/父级可为 null */
  path?: string | null;
  children?: MenuItem[];
}

/**
 * 菜单详情（用于管理）
 */
export interface MenuDetail {
  id: number;
  parentId: number;
  menuName: string;
  menuType: MenuType;
  path?: string;
  component?: string;
  permissionId?: number;
  permission?: {
    id: number;
    permissionCode: string;
    permissionName: string;
  };
  icon?: string;
  sortOrder: number;
  visible: number;
  status: number;
  children?: MenuDetail[];
  createdAt: string;
  updatedAt: string;
}

/**
 * 创建菜单请求
 */
export interface CreateMenuRequest {
  parentId: number;
  menuName: string;
  menuType: MenuType;
  path?: string;
  component?: string;
  permissionId?: number;
  icon?: string;
  sortOrder?: number;
  visible?: number;
  status?: number;
}

/**
 * 更新菜单请求
 */
export interface UpdateMenuRequest {
  parentId?: number;
  menuName?: string;
  menuType?: MenuType;
  path?: string;
  component?: string;
  permissionId?: number;
  icon?: string;
  sortOrder?: number;
  visible?: number;
  status?: number;
}

/** 后端统一包装 ResponseResult<T> */
interface ApiResult<T> {
  code?: number;
  message?: string;
  data?: T;
}

// ============================================
// 菜单导航相关（原有）
// ============================================

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

// ============================================
// 菜单管理相关（新增）
// ============================================

/**
 * 获取所有菜单（树形结构，用于管理）
 */
export async function fetchAllMenus(): Promise<MenuDetail[]> {
  return get<MenuDetail[]>('/menu/all');
}

/**
 * 获取菜单详情
 */
export async function fetchMenu(id: number): Promise<MenuDetail> {
  const r = await get<{ data?: MenuDetail }>(`/menu/${id}`);
  if (!r?.data) throw new Error((r as { message?: string })?.message || '菜单不存在');
  return r.data;
}

/**
 * 创建菜单
 */
export async function createMenu(data: CreateMenuRequest): Promise<number> {
  const response = await post<{ code: number; data: number }>('/menu', data);
  return response.data;
}

/**
 * 更新菜单
 */
export async function updateMenu(id: number, data: UpdateMenuRequest): Promise<void> {
  return put<void>(`/menu/${id}`, data);
}

/**
 * 删除菜单
 */
export async function deleteMenu(id: number): Promise<void> {
  return del<void>(`/menu/${id}`);
}

