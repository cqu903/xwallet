'use client';

import { useMemo, useState } from 'react';
import { Check, Loader2, Search, UserCheck, UserX, Users } from 'lucide-react';
import useSWR from 'swr';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  fetchCustomers,
  toggleCustomerStatus,
  type Customer,
} from '@/lib/api/customers';

const PAGE_SIZE = 10;

const STATUS_MAP: Record<number, {
  label: string;
  variant: 'default' | 'secondary' | 'destructive' | 'outline';
  className?: string;
}> = {
  1: {
    label: '启用',
    variant: 'default',
    className: 'bg-green-500/10 text-green-600 dark:text-green-400 border-green-500/20',
  },
  0: {
    label: '禁用',
    variant: 'destructive',
    className: 'bg-red-500/10 text-red-600 dark:text-red-400 border-red-500/20',
  },
};

const STATUS_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: '1', label: '启用' },
  { value: '0', label: '禁用' },
];

interface FilterState {
  keyword: string;
  status: string;
}

const EMPTY_FILTERS: FilterState = {
  keyword: '',
  status: 'all',
};

function textOrDash(value: unknown): string {
  if (value === null || value === undefined || value === '') return '-';
  return String(value);
}

function formatDateTime(value: unknown): string {
  if (!value) return '-';
  const raw = String(value);
  const parsed = new Date(raw);
  if (Number.isNaN(parsed.getTime())) return raw;
  return parsed.toLocaleString('zh-CN', { hour12: false });
}

function renderStatusBadge(status: number) {
  const mapped = STATUS_MAP[status];
  if (!mapped) return <Badge variant="outline">未知状态</Badge>;
  return (
    <Badge variant={mapped.variant} className={mapped.className}>
      {mapped.label}
    </Badge>
  );
}

function DetailField({ label, value }: { label: string; value: unknown }) {
  return (
    <div className="space-y-1">
      <Label className="text-muted-foreground">{label}</Label>
      <p className="text-sm break-all">{textOrDash(value)}</p>
    </div>
  );
}

export default function CustomersPage() {
  const [page, setPage] = useState(1);
  const [filters, setFilters] = useState<FilterState>(EMPTY_FILTERS);
  const [searchInput, setSearchInput] = useState<FilterState>(EMPTY_FILTERS);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [isToggling, setIsToggling] = useState(false);

  const queryParams = useMemo(() => ({
    page,
    size: PAGE_SIZE,
    keyword: filters.keyword || undefined,
    status: filters.status !== '' && filters.status !== 'all' ? Number(filters.status) : undefined,
  }), [filters, page]);

  const {
    data: customersData,
    isLoading,
    error,
    mutate,
  } = useSWR(
    ['customers', queryParams],
    () => fetchCustomers(queryParams),
    { revalidateOnFocus: false, shouldRetryOnError: false }
  );

  const handleSearch = () => {
    setFilters({ ...searchInput });
    setPage(1);
  };

  const handleReset = () => {
    setSearchInput(EMPTY_FILTERS);
    setFilters(EMPTY_FILTERS);
    setPage(1);
  };

  const handleOpenDetail = (customer: Customer) => {
    setSelectedCustomer(customer);
    setIsDetailOpen(true);
  };

  const handleToggleStatus = async (id: number, currentStatus: number) => {
    const newStatus = currentStatus === 1 ? 0 : 1;
    const confirmMsg = newStatus === 1 ? '确定要启用该顾客吗？' : '确定要禁用该顾客吗？禁用后该顾客将无法登录。';
    if (!confirm(confirmMsg)) return;

    setIsToggling(true);
    try {
      await toggleCustomerStatus(id, newStatus);
      mutate();
    } catch (err) {
      console.error('操作失败:', err);
      alert(err instanceof Error ? err.message : '操作失败');
    } finally {
      setIsToggling(false);
    }
  };

  const customers = customersData?.content ?? [];
  const total = customersData?.totalElements ?? 0;
  const totalPages = customersData?.totalPages ?? 0;

  // 计算当前页的统计数据
  const currentEnabledCount = customers.filter((c) => c.status === 1).length;
  const currentDisabledCount = customers.filter((c) => c.status === 0).length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="relative overflow-hidden rounded-xl border border-border bg-card shadow-sm">
        <div className="absolute inset-0 bg-gradient-to-r from-primary to-primary/90" />
        <div className="relative p-6">
          <h1 className="font-display text-3xl font-bold text-white mb-2">顾客管理</h1>
          <p className="text-white/80">管理系统顾客账号和状态</p>
        </div>
      </div>

      {/* Stat Cards */}
      <div className="grid gap-6 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">总顾客数</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{total}</div>
            <p className="text-xs text-muted-foreground mt-1">全部顾客</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">本页启用</CardTitle>
            <UserCheck className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600 dark:text-green-400">
              {currentEnabledCount}
            </div>
            <p className="text-xs text-muted-foreground mt-1">当前页启用顾客</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">本页禁用</CardTitle>
            <UserX className="h-4 w-4 text-red-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-600 dark:text-red-400">
              {currentDisabledCount}
            </div>
            <p className="text-xs text-muted-foreground mt-1">当前页禁用顾客</p>
          </CardContent>
        </Card>
      </div>

      {/* Search Card */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">查询条件</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-[1fr_auto_auto]">
            <div className="space-y-2">
              <Label htmlFor="keyword">关键词（邮箱/昵称）</Label>
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  id="keyword"
                  placeholder="输入邮箱或昵称"
                  value={searchInput.keyword}
                  onChange={(e) => setSearchInput({ ...searchInput, keyword: e.target.value })}
                  className="pl-10"
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="status">状态</Label>
              <Select
                value={searchInput.status}
                onValueChange={(value) => setSearchInput({ ...searchInput, status: value })}
              >
                <SelectTrigger id="status" className="w-[120px]">
                  <SelectValue placeholder="选择状态" />
                </SelectTrigger>
                <SelectContent>
                  {STATUS_OPTIONS.map((opt) => (
                    <SelectItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-end gap-2">
              <Button onClick={handleSearch} className="min-w-[80px]">
                <Search className="mr-2 h-4 w-4" />
                查询
              </Button>
              <Button variant="outline" onClick={handleReset}>
                重置
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Table Card */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">顾客列表</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow className="bg-muted/30">
                  <TableHead className="w-[80px]">ID</TableHead>
                  <TableHead>邮箱</TableHead>
                  <TableHead>昵称</TableHead>
                  <TableHead className="w-[100px]">状态</TableHead>
                  <TableHead className="w-[180px]">注册时间</TableHead>
                  <TableHead className="w-[180px]">更新时间</TableHead>
                  <TableHead className="text-right w-[180px]">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={7} className="h-24 text-center">
                      <div className="flex items-center justify-center gap-2">
                        <Loader2 className="h-4 w-4 animate-spin" />
                        <span>加载中...</span>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : error ? (
                  <TableRow>
                    <TableCell colSpan={7} className="h-24 text-center text-destructive">
                      加载失败，请重试
                    </TableCell>
                  </TableRow>
                ) : customers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className="h-24 text-center text-muted-foreground">
                      暂无数据
                    </TableCell>
                  </TableRow>
                ) : (
                  customers.map((customer) => (
                    <TableRow key={customer.id} className="hover:bg-muted/50 transition-colors">
                      <TableCell className="font-medium">{customer.id}</TableCell>
                      <TableCell>{textOrDash(customer.email)}</TableCell>
                      <TableCell>{textOrDash(customer.nickname)}</TableCell>
                      <TableCell>{renderStatusBadge(customer.status)}</TableCell>
                      <TableCell>{formatDateTime(customer.createdAt)}</TableCell>
                      <TableCell>{formatDateTime(customer.updatedAt)}</TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleOpenDetail(customer)}
                          >
                            查看
                          </Button>
                          <Button
                            variant={customer.status === 1 ? 'outline' : 'default'}
                            size="sm"
                            onClick={() => handleToggleStatus(customer.id, customer.status)}
                            disabled={isToggling}
                          >
                            {customer.status === 1 ? '禁用' : '启用'}
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>

          {/* Pagination */}
          {total > 0 && (
            <div className="flex items-center justify-between mt-4">
              <div className="text-sm text-muted-foreground">
                共 {total} 条，第 {page} / {totalPages} 页
              </div>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((p) => Math.max(1, p - 1))}
                  disabled={page === 1}
                >
                  上一页
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((p) => p + 1)}
                  disabled={page >= totalPages}
                >
                  下一页
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Detail Dialog */}
      <Dialog open={isDetailOpen} onOpenChange={setIsDetailOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>顾客详情</DialogTitle>
            <DialogDescription>查看顾客的详细信息</DialogDescription>
          </DialogHeader>
          {selectedCustomer && (
            <div className="space-y-4">
              <DetailField label="顾客 ID" value={selectedCustomer.id} />
              <DetailField label="邮箱" value={selectedCustomer.email} />
              <DetailField label="昵称" value={selectedCustomer.nickname} />
              <div className="space-y-1">
                <Label className="text-muted-foreground">状态</Label>
                <div>{renderStatusBadge(selectedCustomer.status)}</div>
              </div>
              <DetailField label="注册时间" value={formatDateTime(selectedCustomer.createdAt)} />
              <DetailField label="更新时间" value={formatDateTime(selectedCustomer.updatedAt)} />
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
