import { get } from './client';

function unwrap<T>(res: { code?: number; message?: string; data?: T }): T {
  if (res?.code !== 200) throw new Error(res?.message || '请求失败');
  return res.data as T;
}

export interface LoanApplicationAdminItem {
  applicationId: number;
  applicationNo: string;
  customerId: number;
  fullName: string;
  status: string;
  riskDecision?: string;
  approvedAmount?: string;
  contractNo?: string;
  contractStatus?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoanApplicationAdminQueryRequest {
  page?: number;
  size?: number;
  applicationNo?: string;
  customerId?: number;
  status?: string;
  riskDecision?: string;
  contractNo?: string;
  contractStatus?: string;
  startTime?: string;
  endTime?: string;
}

export interface LoanApplicationAdminPageResponse {
  list: LoanApplicationAdminItem[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

export interface LoanApplicationAdminDetail {
  applicationId: number;
  applicationNo: string;
  customerId: number;
  status: string;
  productCode?: string;
  approvedAmount?: string;
  fullName?: string;
  hkid?: string;
  homeAddress?: string;
  age?: number;
  occupation?: string;
  monthlyIncome?: string;
  monthlyDebtPayment?: string;
  riskDecision?: string;
  riskReferenceId?: string;
  rejectReason?: string;
  cooldownUntil?: string;
  approvedAt?: string;
  expiresAt?: string;
  signedAt?: string;
  disbursedAt?: string;
  createdAt?: string;
  updatedAt?: string;
  contractNo?: string;
  templateVersion?: string;
  contractStatus?: string;
  digest?: string;
  contractContent?: string;
  contractSignedAt?: string;
}

/**
 * 查询申请记录（GET /admin/loan/applications）
 */
export async function fetchAdminLoanApplications(
  params: LoanApplicationAdminQueryRequest
): Promise<LoanApplicationAdminPageResponse> {
  const queryParams = new URLSearchParams();
  queryParams.append('page', (params.page ?? 1).toString());
  queryParams.append('size', (params.size ?? 10).toString());
  if (params.applicationNo) queryParams.append('applicationNo', params.applicationNo);
  if (params.customerId) queryParams.append('customerId', params.customerId.toString());
  if (params.status) queryParams.append('status', params.status);
  if (params.riskDecision) queryParams.append('riskDecision', params.riskDecision);
  if (params.contractNo) queryParams.append('contractNo', params.contractNo);
  if (params.contractStatus) queryParams.append('contractStatus', params.contractStatus);
  if (params.startTime) queryParams.append('startTime', params.startTime);
  if (params.endTime) queryParams.append('endTime', params.endTime);

  const res = await get<{ code?: number; message?: string; data?: LoanApplicationAdminPageResponse }>(
    `/admin/loan/applications?${queryParams.toString()}`
  );
  return unwrap(res);
}

/**
 * 查询申请详情（GET /admin/loan/applications/{applicationId}）
 */
export async function fetchAdminLoanApplicationDetail(
  applicationId: number
): Promise<LoanApplicationAdminDetail> {
  const res = await get<{ code?: number; message?: string; data?: LoanApplicationAdminDetail }>(
    `/admin/loan/applications/${applicationId}`
  );
  return unwrap(res);
}
