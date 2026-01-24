import { get, post, put, del } from './client';
import { PageResponse } from '@/lib/stores/auth-store';

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

export interface Role {
  id: number;
  roleCode: string;
  roleName: string;
}

export interface CreateUserRequest {
  employeeNo: string;
  username: string;
  email: string;
  password: string;
  roleIds: number[];
}

export interface UpdateUserRequest {
  id: number;
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
 * 获取用户列表
 */
export async function fetchUsers(params: UserQueryRequest): Promise<UserPageResponse> {
  const queryParams = new URLSearchParams();
  queryParams.append('page', params.page.toString());
  queryParams.append('size', params.size.toString());
  if (params.keyword) queryParams.append('keyword', params.keyword);
  if (params.status !== undefined) queryParams.append('status', params.status.toString());

  return get<UserPageResponse>(`/users?${queryParams.toString()}`);
}

/**
 * 获取用户详情
 */
export async function fetchUser(id: number): Promise<User> {
  return get<User>(`/users/${id}`);
}

/**
 * 创建用户
 */
export async function createUser(data: CreateUserRequest): Promise<User> {
  return post<User>('/users', data);
}

/**
 * 更新用户
 */
export async function updateUser(id: number, data: UpdateUserRequest): Promise<User> {
  return put<User>(`/users/${id}`, data);
}

/**
 * 删除用户
 */
export async function deleteUser(id: number): Promise<void> {
  return del<void>(`/users/${id}`);
}

/**
 * 启用/禁用用户
 */
export async function toggleUserStatus(id: number, status: number): Promise<void> {
  return put<void>(`/users/${id}/status`, { status });
}
