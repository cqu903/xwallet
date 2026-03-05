'use client';

import { useState } from 'react';
import useSWR from 'swr';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Label } from '@/components/ui/label';
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
  assignedTo?: number;
}

interface Statistics {
  pending: number;
  inProgress: number;
  contacted: number;
  promised: number;
  paid: number;
  closed: number;
  total: number;
}

const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 'PENDING', label: '待分配' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'CONTACTED', label: '已联系' },
  { value: 'PROMISED', label: '承诺还款' },
  { value: 'PAID', label: '已还清' },
  { value: 'CLOSED', label: '已关闭' },
];

const priorityOptions = [
  { value: '', label: '全部优先级' },
  { value: 'LOW', label: '低' },
  { value: 'MEDIUM', label: '中' },
  { value: 'HIGH', label: '高' },
  { value: 'URGENT', label: '紧急' },
];

const priorityColors: Record<string, string> = {
  LOW: 'bg-gray-500',
  MEDIUM: 'bg-blue-500',
  HIGH: 'bg-orange-500',
  URGENT: 'bg-red-500',
};

export function CollectionTaskList() {
  const [filters, setFilters] = useState({
    status: '',
    priority: '',
  });

  const params = new URLSearchParams();
  if (filters.status) params.append('status', filters.status);
  if (filters.priority) params.append('priority', filters.priority);

  const { data: tasksData } = useSWR<{ data: CollectionTask[] }>(
    `/admin/collection/tasks?${params.toString()}`,
    fetchApi
  );

  const { data: statsData } = useSWR<{ data: Statistics }>(
    '/admin/collection/tasks/statistics',
    fetchApi
  );

  const tasks = tasksData?.data || [];
  const statistics = statsData?.data || {
    pending: 0,
    inProgress: 0,
    contacted: 0,
    promised: 0,
    paid: 0,
    closed: 0,
    total: 0,
  };

  const getPriorityBadge = (priority: string) => {
    return (
      <Badge className={priorityColors[priority] || 'bg-gray-500'}>
        {priorityOptions.find(p => p.value === priority)?.label || priority}
      </Badge>
    );
  };

  const getStatusBadge = (status: string) => {
    const statusLabels: Record<string, string> = {
      PENDING: '待分配',
      IN_PROGRESS: '进行中',
      CONTACTED: '已联系',
      PROMISED: '承诺还款',
      PAID: '已还清',
      CLOSED: '已关闭',
    };
    return <Badge variant="outline">{statusLabels[status] || status}</Badge>;
  };

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-4 gap-4">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">待分配</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{statistics.pending}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">进行中</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{statistics.inProgress}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">已联系</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{statistics.contacted}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">承诺还款</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{statistics.promised}</div>
          </CardContent>
        </Card>
      </div>

      <div className="flex gap-4">
        <div className="flex-1">
          <Label htmlFor="status-filter">状态筛选</Label>
          <select
            id="status-filter"
            className="w-full mt-1 p-2 border rounded"
            value={filters.status}
            onChange={(e) => setFilters({ ...filters, status: e.target.value })}
          >
            {statusOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className="flex-1">
          <Label htmlFor="priority-filter">优先级筛选</Label>
          <select
            id="priority-filter"
            className="w-full mt-1 p-2 border rounded"
            value={filters.priority}
            onChange={(e) => setFilters({ ...filters, priority: e.target.value })}
          >
            {priorityOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="space-y-4">
        {tasks.map((task) => (
          <Card key={task.id}>
            <CardContent className="flex items-center justify-between p-4">
              <div className="flex items-center gap-4">
                {getPriorityBadge(task.priority)}
                <div>
                  <div className="font-semibold">合同: {task.contractNumber}</div>
                  <div className="text-sm text-gray-500">
                    逾期 {task.overdueDays} 天 | ¥{task.overdueTotal.toFixed(2)}
                  </div>
                </div>
                {getStatusBadge(task.status)}
              </div>
              <div className="flex gap-2">
                <Button size="sm" variant="outline">
                  查看详情
                </Button>
                <Button size="sm" variant="outline">
                  添加跟进
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
        {tasks.length === 0 && (
          <div className="text-center text-gray-500 py-8">暂无催收任务</div>
        )}
      </div>
    </div>
  );
}
