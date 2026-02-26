import { fetchApi, get, post, put, del } from './client';

/**
 * 权限资源类型
 */
export type ResourceType = 'MENU' | 'BUTTON' | 'API';

/**
 * 权限接口
 */
export interface Permission {
  id: number;
  permissionCode: string;
  permissionName: string;
  resourceType: ResourceType;
  description?: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * 创建权限请求
 */
export interface CreatePermissionRequest {
  permissionCode: string;
  permissionName: string;
  resourceType: ResourceType;
  description?: string;
}

/**
 * 更新权限请求
 */
export interface UpdatePermissionRequest {
  permissionName: string;
  resourceType: ResourceType;
  description?: string;
  status?: number;
}

/**
 * 获取所有权限
 */
export async function fetchAllPermissions(): Promise<Permission[]> {
  return get<Permission[]>('/permissions/all');
}

/**
 * 获取权限详情
 */
export async function fetchPermission(id: number): Promise<Permission> {
  const r = await get<{ data?: Permission }>(`/permissions/${id}`);
  if (!r?.data) throw new Error((r as { message?: string })?.message || '权限不存在');
  return r.data;
}

/**
 * 创建权限
 */
export async function createPermission(data: CreatePermissionRequest): Promise<number> {
  const response = await post<{ code: number; data: number }>('/permissions', data);
  return response.data;
}

/**
 * 更新权限
 */
export async function updatePermission(id: number, data: UpdatePermissionRequest): Promise<void> {
  return put<void>(`/permissions/${id}`, data);
}

/**
 * 删除权限
 */
export async function deletePermission(id: number): Promise<void> {
  return del<void>(`/permissions/${id}`);
}

/**
 * 获取角色权限列表
 */
export async function fetchRolePermissions(roleId: number): Promise<Permission[]> {
  return get<Permission[]>(`/permissions/role/${roleId}`);
}

/**
 * 为角色分配权限
 */
export async function assignPermissionsToRole(roleId: number, permissionIds: number[]): Promise<void> {
  return put<void>(`/permissions/role/${roleId}`, permissionIds);
}

/**
 * 移除角色权限
 */
export async function removePermissionsFromRole(roleId: number, permissionIds: number[]): Promise<void> {
  return fetchApi<void>(`/permissions/role/${roleId}`, {
    method: 'DELETE',
    body: JSON.stringify(permissionIds),
  });
}
