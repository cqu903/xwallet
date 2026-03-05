'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { fetchApi } from '@/lib/api/client';

interface CollectionTask {
  id: number;
  contractNumber: string;
  overdueDays: number;
  overduePrincipal: number;
  overdueInterest: number;
  overdueTotal: number;
  status: string;
  priority: string;
  lastCalculatedAt: string;
  assignedTo?: number;
  promiseAmount?: number;
  promiseDate?: string;
}

interface CollectionRecord {
  id: number;
  contactMethod: string;
  contactResult: string;
  contactTime: string;
  notes: string;
  nextAction?: string;
  operatorId: number;
}

interface CollectionTaskDetailProps {
  taskId: number;
}

const statusLabels: Record<string, string> = {
  PENDING: '待分配',
  IN_PROGRESS: '进行中',
  CONTACTED: '已联系',
  PROMISED: '承诺还款',
  PAID: '已还清',
  CLOSED: '已关闭',
};

const contactMethodLabels: Record<string, string> = {
  PHONE: '电话',
  SMS: '短信',
  EMAIL: '邮件',
  VISIT: '上门',
  OTHER: '其他',
};

const contactResultLabels: Record<string, string> = {
  NO_ANSWER: '未接通',
  PROMISED: '承诺还款',
  REFUSED: '拒绝还款',
  UNREACHABLE: '无法联系',
  WRONG_NUMBER: '号码错误',
  OTHER: '其他',
};

export function CollectionTaskDetail({ taskId }: CollectionTaskDetailProps) {
  const [task, setTask] = useState<CollectionTask | null>(null);
  const [records, setRecords] = useState<CollectionRecord[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (taskId) {
      fetchTask();
      fetchRecords();
    }
  }, [taskId]);

  const fetchTask = async () => {
    try {
      setLoading(true);
      const data = await fetchApi<{ data: CollectionTask }>(
        `/admin/collection/tasks/${taskId}`
      );
      setTask(data.data);
    } catch (error) {
      console.error('Failed to fetch task:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchRecords = async () => {
    try {
      const data = await fetchApi<{ data: CollectionRecord[] }>(
        `/admin/collection/tasks/${taskId}/records`
      );
      setRecords(data.data || []);
    } catch (error) {
      console.error('Failed to fetch records:', error);
    }
  };

  if (loading) {
    return <div className="text-center py-8">加载中...</div>;
  }

  if (!task) {
    return <div className="text-center py-8 text-red-500">任务不存在</div>;
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      <h1 className="text-3xl font-bold">催收任务详情</h1>

      <Card>
        <CardHeader>
          <CardTitle>基本信息</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <div className="text-sm text-gray-500">合同编号</div>
              <div className="font-semibold">{task.contractNumber}</div>
            </div>
            <div>
              <div className="text-sm text-gray-500">状态</div>
              <Badge variant="outline">{statusLabels[task.status] || task.status}</Badge>
            </div>
            {task.assignedTo && (
              <div>
                <div className="text-sm text-gray-500">负责人ID</div>
                <div>{task.assignedTo}</div>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>逾期情况</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            <div className="flex justify-between">
              <span className="text-gray-500">逾期天数</span>
              <span className="font-semibold">{task.overdueDays} 天</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">逾期本金</span>
              <span>¥{task.overduePrincipal.toFixed(2)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">逾期利息（含罚息）</span>
              <span>¥{task.overdueInterest.toFixed(2)}</span>
            </div>
            <div className="flex justify-between font-bold text-lg">
              <span>逾期总额</span>
              <span className="text-red-600">¥{task.overdueTotal.toFixed(2)}</span>
            </div>
            <div className="text-sm text-gray-400">
              数据更新于: {new Date(task.lastCalculatedAt).toLocaleString('zh-CN')}
            </div>
          </div>
        </CardContent>
      </Card>

      {task.promiseAmount && task.promiseDate && (
        <Card>
          <CardHeader>
            <CardTitle>承诺信息</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-gray-500">承诺金额</span>
                <span className="font-semibold">¥{task.promiseAmount.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">承诺日期</span>
                <span>{new Date(task.promiseDate).toLocaleDateString('zh-CN')}</span>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>跟进记录</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {records.length === 0 ? (
              <div className="text-center text-gray-500 py-4">暂无跟进记录</div>
            ) : (
              records.map((record) => (
                <div key={record.id} className="border-l-2 border-blue-500 pl-4">
                  <div className="text-sm text-gray-500">
                    {new Date(record.contactTime).toLocaleString('zh-CN')}
                  </div>
                  <div className="font-semibold">
                    {contactMethodLabels[record.contactMethod] || record.contactMethod} -{' '}
                    {contactResultLabels[record.contactResult] || record.contactResult}
                  </div>
                  <div className="text-gray-600 mt-1">{record.notes}</div>
                  {record.nextAction && (
                    <div className="text-sm text-blue-600 mt-1">
                      下一步: {record.nextAction}
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
