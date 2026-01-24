/**
 * @xwallet/shared-types
 *
 * xWallet 前后端共享的 TypeScript 类型定义
 * 确保前后端数据结构一致性
 */

// ==================== 用户相关类型 ====================

/**
 * 用户信息
 */
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

/**
 * 角色信息
 */
export interface Role {
  id: number;
  roleCode: string;
  roleName: string;
}

/**
 * 用户状态枚举
 */
export enum UserStatus {
  DISABLED = 0,
  ENABLED = 1,
}

// ==================== 认证相关类型 ====================

/**
 * 登录请求
 */
export interface LoginRequest {
  employeeNo: string;
  password: string;
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string;
  user: User;
  permissions: string[];
}

/**
 * 当前用户上下文
 */
export interface UserContext {
  user: User;
  permissions: string[];
}

// ==================== 菜单相关类型 ====================

/**
 * 菜单项
 */
export interface MenuItem {
  id: string;
  name: string;
  path: string;
  children?: MenuItem[];
}

// ==================== API 响应类型 ====================

/**
 * 通用 API 响应
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

/**
 * 分页请求参数
 */
export interface PageRequest {
  page: number;
  size: number;
  keyword?: string;
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

// ==================== 角色相关类型 ====================

/**
 * 创建角色请求
 */
export interface CreateRoleRequest {
  roleCode: string;
  roleName: string;
  description?: string;
  menuIds: string[];
}

/**
 * 更新角色请求
 */
export interface UpdateRoleRequest {
  id: number;
  roleCode: string;
  roleName: string;
  description?: string;
  menuIds: string[];
}

/**
 * 角色响应
 */
export interface RoleResponse {
  id: number;
  roleCode: string;
  roleName: string;
  description?: string;
  menuIds: string[];
  createdAt: string;
  updatedAt: string;
}

// ==================== 用户管理相关类型 ====================

/**
 * 创建用户请求
 */
export interface CreateUserRequest {
  employeeNo: string;
  username: string;
  email: string;
  password: string;
  roleIds: number[];
}

/**
 * 更新用户请求
 */
export interface UpdateUserRequest {
  id: number;
  username: string;
  email: string;
  roleIds: number[];
}

/**
 * 用户查询请求
 */
export interface UserQueryRequest extends PageRequest {
  employeeNo?: string;
  username?: string;
  email?: string;
  status?: number;
}

// ==================== 操作日志类型 ====================

/**
 * 操作日志
 */
export interface OperationLog {
  id: number;
  userId: number;
  username: string;
  operation: string;
  method: string;
  params: string;
  time: number;
  ip: string;
  createdAt: string;
}
