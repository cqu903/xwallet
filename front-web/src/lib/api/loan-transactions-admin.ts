import { get, post, put } from './client';

function unwrap<T>(res: { code?: number; message?: string; data?: T }): T {
  if (res?.code !== 200) throw new Error(res?.message || '请求失败');
  return res.data as T;
}

export interface LoanTransactionAdminItem {
  transactionId: string;
  type: string;
  status: string;
  occurredAt: string;
  amount: string;
  principalComponent?: string;
  interestComponent?: string;
  customerEmail: string;
  contractId: string;
  idempotencyKey: string;
  source: string;
  createdBy?: string;
  note?: string;
  reversalOf?: string;
}

export interface LoanTransactionAdminQueryRequest {
  page?: number;
  size?: number;
  customerEmail?: string;
  contractNo?: string;
  type?: string;
  status?: string;
  source?: string;
  idempotencyKey?: string;
  createdBy?: string;
  noteKeyword?: string;
  amountMin?: string;
  amountMax?: string;
  startTime?: string;
  endTime?: string;
}

export interface LoanTransactionAdminPageResponse {
  list: LoanTransactionAdminItem[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

export interface LoanTransactionAdminCreateRequest {
  customerEmail: string;
  contractNo: string;
  type: string;
  amount: string;
  idempotencyKey: string;
  note?: string;
}

/**
 * 查询交易记录（GET /admin/loan/transactions）
 */
export async function fetchAdminLoanTransactions(
  params: LoanTransactionAdminQueryRequest
): Promise<LoanTransactionAdminPageResponse> {
  const queryParams = new URLSearchParams();
  queryParams.append('page', (params.page ?? 1).toString());
  queryParams.append('size', (params.size ?? 10).toString());
  if (params.customerEmail) queryParams.append('customerEmail', params.customerEmail);
  if (params.contractNo) queryParams.append('contractNo', params.contractNo);
  if (params.type) queryParams.append('type', params.type);
  if (params.status) queryParams.append('status', params.status);
  if (params.source) queryParams.append('source', params.source);
  if (params.idempotencyKey) queryParams.append('idempotencyKey', params.idempotencyKey);
  if (params.createdBy) queryParams.append('createdBy', params.createdBy);
  if (params.noteKeyword) queryParams.append('noteKeyword', params.noteKeyword);
  if (params.amountMin) queryParams.append('amountMin', params.amountMin);
  if (params.amountMax) queryParams.append('amountMax', params.amountMax);
  if (params.startTime) queryParams.append('startTime', params.startTime);
  if (params.endTime) queryParams.append('endTime', params.endTime);

  const res = await get<{ code?: number; message?: string; data?: LoanTransactionAdminPageResponse }>(
    `/admin/loan/transactions?${queryParams.toString()}`
  );
  return unwrap(res);
}

/**
 * 创建运营交易（POST /admin/loan/transactions）
 */
export async function createAdminLoanTransaction(
  data: LoanTransactionAdminCreateRequest
): Promise<LoanTransactionAdminItem> {
  const res = await post<{ code?: number; message?: string; data?: LoanTransactionAdminItem }>(
    '/admin/loan/transactions',
    data
  );
  return unwrap(res);
}

/**
 * 更新交易备注（PUT /admin/loan/transactions/{txnNo}/note）
 */
export async function updateAdminLoanTransactionNote(txnNo: string, note: string): Promise<void> {
  const res = await put<{ code?: number; message?: string }>(
    `/admin/loan/transactions/${txnNo}/note`,
    { note }
  );
  unwrap(res);
}

/**
 * 冲正交易（POST /admin/loan/transactions/{txnNo}/reversal）
 */
export async function reverseAdminLoanTransaction(txnNo: string, note?: string): Promise<void> {
  const res = await post<{ code?: number; message?: string }>(
    `/admin/loan/transactions/${txnNo}/reversal`,
    note ? { note } : {}
  );
  unwrap(res);
}
