import { get } from './client';

function unwrap<T>(res: { code?: number; message?: string; data?: T }): T {
  if (res?.code !== 200) throw new Error(res?.message || '请求失败');
  return res.data as T;
}

export interface AnalyticsEvent {
  id: number;
  eventId: string;
  deviceId: string;
  userId?: string;
  eventType: string;
  environment: string;
  topic: string;
  payload: string;
  appVersion?: string;
  os?: string;
  osVersion?: string;
  deviceModel?: string;
  networkType?: string;
  sessionId?: string;
  isCritical: boolean;
  receivedAt: number;
  eventTimestamp: number;
  createdAt: string;
  updatedAt: string;
}

export interface AnalyticsEventQueryRequest {
  page?: number;
  size?: number;
  userId?: string;
  deviceId?: string;
  eventType?: string;
  environment?: string;
  isCritical?: boolean;
  startTime?: string;
  endTime?: string;
}

export interface AnalyticsEventPageResponse {
  list: AnalyticsEvent[];
  total: number;
  page: number;
  size: number;
}

/**
 * 获取事件列表（GET /analytics/events/list）
 */
export async function fetchAnalyticsEvents(params: AnalyticsEventQueryRequest): Promise<AnalyticsEventPageResponse> {
  const queryParams = new URLSearchParams();
  queryParams.append('page', (params.page ?? 0).toString());
  queryParams.append('size', (params.size ?? 20).toString());
  if (params.userId) queryParams.append('userId', params.userId);
  if (params.deviceId) queryParams.append('deviceId', params.deviceId);
  if (params.eventType) queryParams.append('eventType', params.eventType);
  if (params.environment) queryParams.append('environment', params.environment);
  if (params.isCritical !== undefined) queryParams.append('isCritical', params.isCritical.toString());
  if (params.startTime) queryParams.append('startTime', params.startTime);
  if (params.endTime) queryParams.append('endTime', params.endTime);

  const res = await get<{ code?: number; message?: string; data?: AnalyticsEventPageResponse }>(
    `/analytics/events/list?${queryParams.toString()}`
  );
  return unwrap(res);
}

/**
 * 获取事件详情（GET /analytics/events/{eventId}）
 */
export async function fetchAnalyticsEvent(eventId: string): Promise<AnalyticsEvent> {
  const res = await get<{ code?: number; message?: string; data?: AnalyticsEvent }>(`/analytics/events/${eventId}`);
  return unwrap(res);
}
