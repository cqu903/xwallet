import { get, post, put, del } from './client';
import type { Role } from './roles';

function unwrap<T>(res: { code?: number; message?: string; data?: T }): T {
  if (res?.code !== 200) throw new Error(res?.message || '请求失败');
  return res.data as T;
}

export interface User {
  id: number;
  employeeNo: string;
  username: string;
  email: string;
  status: number;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  employeeNo: string;
  username: string;
  email: string;
  password: string;
  roleIds: number[];
}

export interface UpdateUserRequest {
  username: string;
  email: string;
  roleIds: number[];
}

export interface UserQueryRequest {
  page: number;
  size: number;
  keyword?: string;
  status?: number;
}

export interface UserPageResponse {
  content: User[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

/**
 * 获取用户列表（GET /user/list，返回 list/total 映射为 content/totalElements）
 */
export async function fetchUsers(params: UserQueryRequest): Promise<UserPageResponse> {
  const queryParams = new URLSearchParams();
  queryParams.append('page', params.page.toString());
  queryParams.append('size', params.size.toString());
  if (params.keyword) queryParams.append('keyword', params.keyword);
  if (params.status !== undefined) queryParams.append('status', params.status.toString());

  const res = await get<{ code?: number; message?: string; data?: { list?: User[]; total?: number; page?: number; size?: number; totalPages?: number } }>(
    `/user/list?${queryParams.toString()}`
  );
  const d = unwrap(res);
  return {
    content: d?.list ?? [],
    totalElements: d?.total ?? 0,
    totalPages: d?.totalPages ?? 0,
    page: d?.page ?? 1,
    size: d?.size ?? 10,
  };
}

/**
 * 获取用户详情（GET /user/{id}）
 */
export async function fetchUser(id: number): Promise<User> {
  const res = await get<{ code?: number; message?: string; data?: User }>(`/user/${id}`);
  return unwrap(res) as User;
}

/**
 * 创建用户（POST /user，需 user:create 权限）
 */
export async function createUser(data: CreateUserRequest): Promise<void> {
  const res = await post<{ code?: number; message?: string }>('/user', data);
  unwrap(res);
}

/**
 * 更新用户（PUT /user/{id}）
 */
export async function updateUser(id: number, data: UpdateUserRequest): Promise<void> {
  const res = await put<{ code?: number; message?: string }>(`/user/${id}`, data);
  unwrap(res);
}

/**
 * 删除用户（DELETE /user/{id}）
 * 注意：当前后端未实现该接口，调用会 404。
 */
export async function deleteUser(id: number): Promise<void> {
  const res = await del<{ code?: number; message?: string }>(`/user/${id}`);
  unwrap(res);
}

/**
 * 启用/禁用用户（PUT /user/{id}/status?status=）
 */
export async function toggleUserStatus(id: number, status: number): Promise<void> {
  const res = await put<{ code?: number; message?: string }>(`/user/${id}/status?status=${status}`, undefined);
  unwrap(res);
}
