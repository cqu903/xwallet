import { get, post, put, del } from './client';

export interface Role {
  id: number;
  roleCode: string;
  roleName: string;
  description?: string;
  status: number;
  sortOrder?: number;
  menuIds?: number[];
  userCount?: number;
}

export interface CreateRoleRequest {
  roleCode: string;
  roleName: string;
  description?: string;
  status?: number;
  menuIds: number[];
}

export interface UpdateRoleRequest {
  roleName: string;
  description?: string;
  status?: number;
  menuIds: number[];
}

export interface RoleDetail {
  id: number;
  roleCode: string;
  roleName: string;
  description?: string;
  status?: number;
  menuIds: number[];
}

/**
 * 获取角色列表
 */
export async function fetchRoles(): Promise<Role[]> {
  return get<Role[]>('/role/list');
}

/**
 * 获取角色详情（解包 ResponseResult）
 */
export async function fetchRole(id: number): Promise<RoleDetail> {
  const r = await get<{ data?: RoleDetail }>(`/role/${id}`);
  if (!r?.data) throw new Error((r as { message?: string })?.message || '角色不存在');
  return r.data;
}

/**
 * 创建角色
 */
export async function createRole(data: CreateRoleRequest): Promise<number> {
  const response = await post<{ code: number; data: number }>('/role', data);
  return response.data;
}

/**
 * 更新角色
 */
export async function updateRole(id: number, data: UpdateRoleRequest): Promise<void> {
  return put<void>(`/role/${id}`, data);
}

/**
 * 切换角色状态（后端为 PUT /role/{id}/status?status=）
 */
export async function toggleRoleStatus(id: number, status: number): Promise<void> {
  return put<void>(`/role/${id}/status?status=${status}`);
}

/**
 * 删除角色
 */
export async function deleteRole(id: number): Promise<void> {
  return del<void>(`/role/${id}`);
}
