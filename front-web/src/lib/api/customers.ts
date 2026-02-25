import { get, put } from './client';

function unwrap<T>(res: { code?: number; message?: string; data?: T }): T {
  if (res?.code !== 200) throw new Error(res?.message || '请求失败');
  return res.data as T;
}

export interface Customer {
  id: number;
  email: string;
  nickname: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerQueryRequest {
  page: number;
  size: number;
  keyword?: string;
  status?: number;
}

export interface CustomerPageResponse {
  content: Customer[];
  totalElements: number;
  page: number;
  size: number;
  totalPages: number;
}

/**
 * 获取顾客列表（GET /admin/customer/list）
 */
export async function fetchCustomers(params: CustomerQueryRequest): Promise<CustomerPageResponse> {
  const queryParams = new URLSearchParams();
  queryParams.append('page', params.page.toString());
  queryParams.append('size', params.size.toString());
  if (params.keyword) queryParams.append('keyword', params.keyword);
  if (params.status !== undefined) queryParams.append('status', params.status.toString());

  const res = await get<{ code?: number; message?: string; data?: {
    content?: Customer[];
    totalElements?: number;
    page?: number;
    size?: number;
    totalPages?: number;
  } }>(`/admin/customer/list?${queryParams.toString()}`);

  const d = unwrap(res);
  return {
    content: d?.content ?? [],
    totalElements: d?.totalElements ?? 0,
    page: d?.page ?? 1,
    size: d?.size ?? 10,
    totalPages: d?.totalPages ?? 0,
  };
}

/**
 * 获取顾客详情（GET /admin/customer/{id}）
 */
export async function fetchCustomer(id: number): Promise<Customer> {
  const res = await get<{ code?: number; message?: string; data?: Customer }>(`/admin/customer/${id}`);
  return unwrap(res);
}

/**
 * 启用/禁用顾客（PUT /admin/customer/{id}/status?status=）
 */
export async function toggleCustomerStatus(id: number, status: number): Promise<void> {
  const res = await put<{ code?: number; message?: string }>(`/admin/customer/${id}/status?status=${status}`, undefined);
  unwrap(res);
}
