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
 * 获取角色详情
 */
export async function fetchRole(id: number): Promise<RoleDetail> {
  return get<RoleDetail>(`/role/${id}`);
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
 * 切换角色状态
 */
export async function toggleRoleStatus(id: number, status: number): Promise<void> {
  return get<void>(`/role/${id}/status?status=${status}`);
}

/**
 * 删除角色
 */
export async function deleteRole(id: number): Promise<void> {
  return del<void>(`/role/${id}`);
}
