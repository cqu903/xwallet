'use client';

import { useState, useEffect } from 'react';
import { useTranslations } from 'next-intl';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { fetchAnalyticsEvents, type AnalyticsEvent } from '@/lib/api/analytics';
import useSWR from 'swr';
import { Search, Activity, Clock, AlertCircle, CheckCircle } from 'lucide-react';

export default function MqttEventsPage() {
  const t = useTranslations();
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({
    userId: '',
    deviceId: '',
    eventType: '',
    environment: '',
    isCritical: '',
  });
  const [searchInput, setSearchInput] = useState({
    userId: '',
    deviceId: '',
    eventType: '',
  });
  const [isDetailDialogOpen, setIsDetailDialogOpen] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState<AnalyticsEvent | null>(null);

  // 获取事件列表
  const { data: eventsData, isLoading, mutate } = useSWR(
    ['analytics-events', page, filters],
    () => fetchAnalyticsEvents({ ...filters, page, size: 20 }),
    { revalidateOnFocus: false, shouldRetryOnError: false }
  );

  useEffect(() => {
    mutate();
  }, []);

  const handleSearch = () => {
    setFilters({
      ...searchInput,
      environment: filters.environment,
      isCritical: filters.isCritical,
    });
    setPage(0);
  };

  const handleReset = () => {
    setSearchInput({ userId: '', deviceId: '', eventType: '' });
    setFilters({ userId: '', deviceId: '', eventType: '', environment: '', isCritical: '' });
    setPage(0);
  };

  const handleViewDetail = (event: AnalyticsEvent) => {
    setSelectedEvent(event);
    setIsDetailDialogOpen(true);
  };

  const formatTimestamp = (timestamp: number) => {
    return new Date(timestamp).toLocaleString('zh-CN');
  };

  const getEventTypeBadge = (eventType: string, isCritical: boolean) => {
    if (isCritical) {
      return (
        <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200">
          <AlertCircle className="w-3 h-3 mr-1" />
          {eventType}
        </span>
      );
    }
    return (
      <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">
        <Activity className="w-3 h-3 mr-1" />
        {eventType}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="w-5 h-5" />
            MQTT事件管理
          </CardTitle>
        </CardHeader>
        <CardContent>
          {/* 筛选器 */}
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-6">
            <div className="space-y-2">
              <Label htmlFor="userId">用户ID</Label>
              <Input
                id="userId"
                placeholder="输入用户ID"
                value={searchInput.userId}
                onChange={(e) => setSearchInput({ ...searchInput, userId: e.target.value })}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="deviceId">设备ID</Label>
              <Input
                id="deviceId"
                placeholder="输入设备ID"
                value={searchInput.deviceId}
                onChange={(e) => setSearchInput({ ...searchInput, deviceId: e.target.value })}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="eventType">事件类型</Label>
              <Input
                id="eventType"
                placeholder="输入事件类型"
                value={searchInput.eventType}
                onChange={(e) => setSearchInput({ ...searchInput, eventType: e.target.value })}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="environment">环境</Label>
              <Select
                value={filters.environment || "all"}
                onValueChange={(value) => setFilters({ ...filters, environment: value === "all" ? "" : value })}
              >
                <SelectTrigger id="environment">
                  <SelectValue placeholder="选择环境" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部</SelectItem>
                  <SelectItem value="prod">生产环境</SelectItem>
                  <SelectItem value="dev">开发环境</SelectItem>
                  <SelectItem value="test">测试环境</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="isCritical">事件类型</Label>
              <Select
                value={filters.isCritical || "all"}
                onValueChange={(value) => setFilters({ ...filters, isCritical: value === "all" ? "" : value })}
              >
                <SelectTrigger id="isCritical">
                  <SelectValue placeholder="全部事件" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部事件</SelectItem>
                  <SelectItem value="true">关键事件</SelectItem>
                  <SelectItem value="false">普通事件</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* 搜索按钮 */}
          <div className="flex gap-2 mb-4">
            <Button onClick={handleSearch} className="gap-2">
              <Search className="w-4 h-4" />
              搜索
            </Button>
            <Button onClick={handleReset} variant="outline">
              重置
            </Button>
          </div>

          {/* 事件列表 */}
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-[150px]">事件ID</TableHead>
                  <TableHead>事件类型</TableHead>
                  <TableHead>用户ID</TableHead>
                  <TableHead>设备ID</TableHead>
                  <TableHead>环境</TableHead>
                  <TableHead>接收时间</TableHead>
                  <TableHead className="text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center py-8">
                      加载中...
                    </TableCell>
                  </TableRow>
                ) : eventsData?.list?.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center py-8">
                      暂无数据
                    </TableCell>
                  </TableRow>
                ) : (
                  eventsData?.list?.map((event) => (
                    <TableRow key={event.id}>
                      <TableCell className="font-mono text-xs">
                        {event.eventId.slice(0, 8)}...
                      </TableCell>
                      <TableCell>
                        {getEventTypeBadge(event.eventType, event.isCritical)}
                      </TableCell>
                      <TableCell>{event.userId || '-'}</TableCell>
                      <TableCell className="font-mono text-xs">
                        {event.deviceId.slice(0, 12)}...
                      </TableCell>
                      <TableCell>
                        <span className={`inline-flex items-center px-2 py-1 rounded text-xs font-medium ${
                          event.environment === 'prod'
                            ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                            : 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200'
                        }`}>
                          {event.environment}
                        </span>
                      </TableCell>
                      <TableCell className="text-sm text-muted-foreground">
                        <div className="flex items-center gap-1">
                          <Clock className="w-3 h-3" />
                          {formatTimestamp(event.receivedAt)}
                        </div>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleViewDetail(event)}
                        >
                          详情
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>

          {/* 分页 */}
          {eventsData && eventsData.total > 0 && (
            <div className="flex items-center justify-between mt-4">
              <div className="text-sm text-muted-foreground">
                共 {eventsData.total} 条记录，第 {page + 1} / {Math.ceil(eventsData.total / eventsData.size)} 页
              </div>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                >
                  上一页
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((p) => p + 1)}
                  disabled={(page + 1) * eventsData.size >= eventsData.total}
                >
                  下一页
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* 详情弹窗 */}
      <Dialog open={isDetailDialogOpen} onOpenChange={setIsDetailDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>事件详情</DialogTitle>
            <DialogDescription>
              查看完整的 MQTT 事件信息和 Payload
            </DialogDescription>
          </DialogHeader>
          {selectedEvent && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-muted-foreground">事件ID</Label>
                  <p className="font-mono text-sm">{selectedEvent.eventId}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">事件类型</Label>
                  <p>{selectedEvent.eventType}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">用户ID</Label>
                  <p>{selectedEvent.userId || '-'}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">设备ID</Label>
                  <p className="font-mono text-sm">{selectedEvent.deviceId}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">环境</Label>
                  <p>{selectedEvent.environment}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">是否关键事件</Label>
                  <p>{selectedEvent.isCritical ? '是' : '否'}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">App版本</Label>
                  <p>{selectedEvent.appVersion || '-'}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">操作系统</Label>
                  <p>{selectedEvent.os || '-'} {selectedEvent.osVersion || ''}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">设备型号</Label>
                  <p>{selectedEvent.deviceModel || '-'}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">网络类型</Label>
                  <p>{selectedEvent.networkType || '-'}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">会话ID</Label>
                  <p className="font-mono text-sm">{selectedEvent.sessionId || '-'}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Topic</Label>
                  <p className="font-mono text-sm">{selectedEvent.topic}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">事件时间</Label>
                  <p>{formatTimestamp(selectedEvent.eventTimestamp)}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">接收时间</Label>
                  <p>{formatTimestamp(selectedEvent.receivedAt)}</p>
                </div>
              </div>

              <div>
                <Label className="text-muted-foreground">完整 Payload (JSON)</Label>
                <pre className="mt-2 p-4 bg-muted rounded-md overflow-x-auto text-xs">
                  {JSON.stringify(JSON.parse(selectedEvent.payload), null, 2)}
                </pre>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
