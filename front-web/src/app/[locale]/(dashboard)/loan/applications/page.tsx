'use client';

import { Fragment, useMemo, useState } from 'react';
import { Check, FileText, Search, RefreshCw, ChevronDown, ChevronUp } from 'lucide-react';
import useSWR from 'swr';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible';
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
  fetchAdminLoanApplicationDetail,
  fetchAdminLoanApplications,
  type LoanApplicationAdminDetail,
  type LoanApplicationAdminItem,
} from '@/lib/api/loan-applications-admin';
import { cn } from '@/lib/utils';

const PAGE_SIZE = 10;

const APPLICATION_STATUS_MAP: Record<string, {
  label: string;
  variant: 'default' | 'secondary' | 'destructive' | 'outline';
  className?: string;
  style?: React.CSSProperties;
}> = {
  SUBMITTED: {
    label: '已提交',
    variant: 'outline',
    style: {
      backgroundColor: 'oklch(55% 0.22 260 / 0.1)',
      color: 'oklch(55% 0.22 260)',
      borderColor: 'oklch(55% 0.22 260 / 0.2)',
    },
  },
  REJECTED: {
    label: '已拒绝',
    variant: 'destructive',
  },
  APPROVED_PENDING_SIGN: {
    label: '待签署',
    variant: 'outline',
    className: 'animate-pulse',
    style: {
      backgroundColor: 'oklch(70% 0.15 60 / 0.1)',
      color: 'oklch(70% 0.15 60)',
      borderColor: 'oklch(70% 0.15 60 / 0.2)',
    },
  },
  SIGNED: {
    label: '已签署',
    variant: 'secondary',
  },
  DISBURSED: {
    label: '已放款',
    variant: 'outline',
    style: {
      background: 'linear-gradient(to right, oklch(55% 0.18 145 / 0.2), oklch(55% 0.18 165 / 0.2))',
      color: 'oklch(45% 0.18 155)',
      borderColor: 'oklch(55% 0.18 155 / 0.3)',
    },
  },
  EXPIRED: {
    label: '已过期',
    variant: 'secondary',
    style: {
      backgroundColor: 'oklch(50% 0 0 / 0.1)',
      color: 'oklch(50% 0 0)',
      borderColor: 'oklch(50% 0 0 / 0.2)',
    },
  },
};

const CONTRACT_STATUS_MAP: Record<string, {
  label: string;
  variant: 'default' | 'secondary' | 'destructive' | 'outline';
  className?: string;
  style?: React.CSSProperties;
}> = {
  DRAFT: {
    label: '草稿',
    variant: 'outline',
    style: {
      backgroundColor: 'oklch(50% 0.02 265 / 0.1)',
      color: 'oklch(45% 0.02 265)',
      borderColor: 'oklch(50% 0.02 265 / 0.2)',
    },
  },
  SIGNED: {
    label: '已签署',
    variant: 'default',
    style: {
      backgroundColor: 'oklch(55% 0.18 145 / 0.1)',
      color: 'oklch(45% 0.18 145)',
      borderColor: 'oklch(55% 0.18 145 / 0.3)',
    },
  },
};

const RISK_DECISION_MAP: Record<string, {
  label: string;
  variant: 'default' | 'secondary' | 'destructive' | 'outline';
  className?: string;
  style?: React.CSSProperties;
}> = {
  APPROVED: {
    label: '通过',
    variant: 'default',
    style: {
      backgroundColor: 'oklch(55% 0.18 165 / 0.1)',
      color: 'oklch(45% 0.18 165)',
      borderColor: 'oklch(55% 0.18 165 / 0.3)',
    },
  },
  REJECTED: {
    label: '拒绝',
    variant: 'destructive',
  },
};

const APPLICATION_STATUS_OPTIONS = [
  { value: 'SUBMITTED', label: '已提交' },
  { value: 'REJECTED', label: '已拒绝' },
  { value: 'APPROVED_PENDING_SIGN', label: '待签署' },
  { value: 'SIGNED', label: '已签署' },
  { value: 'DISBURSED', label: '已放款' },
  { value: 'EXPIRED', label: '已过期' },
];

const CONTRACT_STATUS_OPTIONS = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'SIGNED', label: '已签署' },
];

const RISK_DECISION_OPTIONS = [
  { value: 'APPROVED', label: '通过' },
  { value: 'REJECTED', label: '拒绝' },
];

interface FilterState {
  applicationNo: string;
  customerId: string;
  status: string;
  riskDecision: string;
  contractNo: string;
  contractStatus: string;
  startTime: string;
  endTime: string;
}

const EMPTY_FILTERS: FilterState = {
  applicationNo: '',
  customerId: '',
  status: '',
  riskDecision: '',
  contractNo: '',
  contractStatus: '',
  startTime: '',
  endTime: '',
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

function formatAmount(value: unknown): string {
  if (value === null || value === undefined || value === '') return '-';
  const raw = String(value);
  const parsed = Number(raw);
  if (Number.isNaN(parsed)) return raw;
  return parsed.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function renderEnumBadge(
  value: unknown,
  map: Record<string, {
    label: string;
    variant: 'default' | 'secondary' | 'destructive' | 'outline';
    className?: string;
    style?: React.CSSProperties;
  }>
) {
  const raw = textOrDash(value);
  if (raw === '-') return raw;
  const mapped = map[raw];
  if (!mapped) return <Badge variant="outline">{raw}</Badge>;
  return (
    <Badge
      variant={mapped.variant}
      className={cn("font-medium", mapped.className)}
      style={mapped.style}
    >
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

export default function LoanApplicationsPage() {
  const [page, setPage] = useState(1);
  const [filters, setFilters] = useState<FilterState>(EMPTY_FILTERS);
  const [searchInput, setSearchInput] = useState<FilterState>(EMPTY_FILTERS);
  const [selectedApplication, setSelectedApplication] = useState<LoanApplicationAdminItem | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [isAdvancedFilterOpen, setIsAdvancedFilterOpen] = useState(false);

  const queryParams = useMemo(() => {
    const customerId = filters.customerId.trim() ? Number(filters.customerId.trim()) : undefined;
    return {
      page,
      size: PAGE_SIZE,
      applicationNo: filters.applicationNo || undefined,
      customerId: Number.isNaN(customerId) ? undefined : customerId,
      status: filters.status || undefined,
      riskDecision: filters.riskDecision || undefined,
      contractNo: filters.contractNo || undefined,
      contractStatus: filters.contractStatus || undefined,
      startTime: filters.startTime || undefined,
      endTime: filters.endTime || undefined,
    };
  }, [filters, page]);

  const {
    data: applicationsData,
    isLoading,
    error,
    mutate,
  } = useSWR(
    ['admin-loan-applications', queryParams],
    () => fetchAdminLoanApplications(queryParams),
    { revalidateOnFocus: false, shouldRetryOnError: false }
  );

  const selectedApplicationId = selectedApplication?.applicationId;
  const shouldFetchDetail = isDetailOpen && !!selectedApplicationId;
  const {
    data: detailData,
    isLoading: detailLoading,
    error: detailError,
    mutate: mutateDetail,
  } = useSWR(
    shouldFetchDetail ? ['admin-loan-application-detail', selectedApplicationId] : null,
    () => fetchAdminLoanApplicationDetail(selectedApplicationId as number),
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

  const handleOpenDetail = (application: LoanApplicationAdminItem) => {
    setSelectedApplication(application);
    setIsDetailOpen(true);
  };

  const handleDetailOpenChange = (open: boolean) => {
    setIsDetailOpen(open);
    if (!open) setSelectedApplication(null);
  };

  const detail: LoanApplicationAdminDetail | undefined = detailData;

  const timelineItems = useMemo(() => {
    if (!detail) return [];
    return [
      { label: '提交', completed: !!detail.createdAt },
      { label: '审批', completed: !!detail.approvedAt },
      { label: '签署', completed: !!detail.signedAt },
      { label: '放款', completed: !!detail.disbursedAt },
    ];
  }, [detail]);

  return (
    <div className="space-y-6 animate-fade-in">
      {/* 装饰性背景 */}
      <div className="relative overflow-hidden rounded-2xl border border-primary/20">
        <div className="absolute inset-0 bg-gradient-to-br from-primary/10 to-primary/5" />
        <div className="absolute inset-0 bg-grid opacity-10" />
        <Card className="relative glass">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="w-5 h-5" />
            贷款申请单据管理
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <div className="space-y-2">
              <Label htmlFor="applicationNo">申请编号</Label>
              <Input
                id="applicationNo"
                placeholder="输入申请编号"
                value={searchInput.applicationNo}
                onChange={(e) => setSearchInput({ ...searchInput, applicationNo: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="customerId">客户ID</Label>
              <Input
                id="customerId"
                placeholder="输入客户ID"
                value={searchInput.customerId}
                onChange={(e) => setSearchInput({ ...searchInput, customerId: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="status">申请状态</Label>
              <Select
                value={searchInput.status || 'all'}
                onValueChange={(value) => setSearchInput({ ...searchInput, status: value === 'all' ? '' : value })}
              >
                <SelectTrigger id="status">
                  <SelectValue placeholder="选择状态" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部</SelectItem>
                  {APPLICATION_STATUS_OPTIONS.map((item) => (
                    <SelectItem key={item.value} value={item.value}>
                      {item.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="riskDecision">风控决策</Label>
              <Select
                value={searchInput.riskDecision || 'all'}
                onValueChange={(value) =>
                  setSearchInput({ ...searchInput, riskDecision: value === 'all' ? '' : value })
                }
              >
                <SelectTrigger id="riskDecision">
                  <SelectValue placeholder="选择决策" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部</SelectItem>
                  {RISK_DECISION_OPTIONS.map((item) => (
                    <SelectItem key={item.value} value={item.value}>
                      {item.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* 高级筛选 */}
          <Collapsible open={isAdvancedFilterOpen} onOpenChange={setIsAdvancedFilterOpen}>
            <CollapsibleContent className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6 space-y-0">
              <div className="space-y-2">
                <Label htmlFor="contractNo">合同号</Label>
                <Input
                  id="contractNo"
                  placeholder="输入合同号"
                  value={searchInput.contractNo}
                  onChange={(e) => setSearchInput({ ...searchInput, contractNo: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="contractStatus">合同状态</Label>
                <Select
                  value={searchInput.contractStatus || 'all'}
                  onValueChange={(value) =>
                    setSearchInput({ ...searchInput, contractStatus: value === 'all' ? '' : value })
                  }
                >
                  <SelectTrigger id="contractStatus">
                    <SelectValue placeholder="选择合同状态" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">全部</SelectItem>
                    {CONTRACT_STATUS_OPTIONS.map((item) => (
                      <SelectItem key={item.value} value={item.value}>
                        {item.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="startTime">开始日期</Label>
                <Input
                  id="startTime"
                  type="date"
                  value={searchInput.startTime}
                  onChange={(e) => setSearchInput({ ...searchInput, startTime: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="endTime">结束日期</Label>
                <Input
                  id="endTime"
                  type="date"
                  value={searchInput.endTime}
                  onChange={(e) => setSearchInput({ ...searchInput, endTime: e.target.value })}
                />
              </div>
            </CollapsibleContent>
          </Collapsible>

          <div className="flex gap-2 mb-4">
            <Button
              onClick={handleSearch}
              className="gap-2 bg-gradient-to-r from-primary to-primary/90 hover:from-primary/90
                         hover:to-primary shadow-md btn-shine"
            >
              <Search className="w-4 h-4" />
              搜索
            </Button>
            <Button
              onClick={handleReset}
              variant="outline"
              className="hover:bg-primary/10 hover:border-primary/30 transition-all duration-200"
            >
              重置
            </Button>
            <Button
              className="ml-auto gap-2 hover:bg-primary/10 transition-all duration-200"
              variant="outline"
              onClick={() => mutate()}
            >
              <RefreshCw className="w-4 h-4" />
              刷新
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setIsAdvancedFilterOpen(!isAdvancedFilterOpen)}
              className="gap-2"
            >
              {isAdvancedFilterOpen ? (
                <>
                  <ChevronUp className="w-4 h-4" />
                  收起筛选
                </>
              ) : (
                <>
                  <ChevronDown className="w-4 h-4" />
                  高级筛选
                </>
              )}
            </Button>
          </div>

          <div className="rounded-xl border border-border/50 overflow-hidden bg-card/50 backdrop-blur-sm">
            <Table>
              <TableHeader className="bg-muted/30">
                <TableRow>
                  <TableHead className="w-[160px]">申请编号</TableHead>
                  <TableHead>客户</TableHead>
                  <TableHead>申请状态</TableHead>
                  <TableHead>风控决策</TableHead>
                  <TableHead>核准金额</TableHead>
                  <TableHead>合同状态</TableHead>
                  <TableHead>创建时间</TableHead>
                  <TableHead>更新时间</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center py-8">
                      加载中...
                    </TableCell>
                  </TableRow>
                ) : error ? (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center py-8 text-destructive">
                      加载失败，请重试
                    </TableCell>
                  </TableRow>
                ) : applicationsData?.list?.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center py-8">
                      暂无申请记录
                    </TableCell>
                  </TableRow>
                ) : (
                  applicationsData?.list?.map((item, index) => (
                    <TableRow
                      key={item.applicationId}
                      role="button"
                      tabIndex={0}
                      className={cn(
                        "transition-colors hover:bg-primary/5 cursor-pointer",
                        index % 2 === 0 ? "bg-transparent" : "bg-muted/20"
                      )}
                      onClick={() => handleOpenDetail(item)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                          e.preventDefault();
                          handleOpenDetail(item);
                        }
                      }}
                    >
                      <TableCell className="font-mono text-xs">{item.applicationNo}</TableCell>
                      <TableCell>{item.customerId} / {item.fullName}</TableCell>
                      <TableCell>{renderEnumBadge(item.status, APPLICATION_STATUS_MAP)}</TableCell>
                      <TableCell>{renderEnumBadge(item.riskDecision, RISK_DECISION_MAP)}</TableCell>
                      <TableCell>{formatAmount(item.approvedAmount)}</TableCell>
                      <TableCell>{renderEnumBadge(item.contractStatus, CONTRACT_STATUS_MAP)}</TableCell>
                      <TableCell>{formatDateTime(item.createdAt)}</TableCell>
                      <TableCell>{formatDateTime(item.updatedAt)}</TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>

          {applicationsData && applicationsData.total > 0 && (
            <div className="flex items-center justify-between mt-4">
              <div className="text-sm text-muted-foreground">
                共 {applicationsData.total} 条记录，第 {page} / {Math.ceil(applicationsData.total / applicationsData.size)} 页
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
                  disabled={page * applicationsData.size >= applicationsData.total}
                >
                  下一页
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
      </div>

      <Dialog open={isDetailOpen} onOpenChange={handleDetailOpenChange}>
        <DialogContent className="left-auto right-0 top-0 h-full max-h-none w-full max-w-2xl
                    translate-x-0 translate-y-0 overflow-y-auto rounded-none sm:rounded-none
                    bg-gradient-to-b from-background to-muted/20">
          <DialogHeader className="border-b border-border/50 pb-4 bg-gradient-to-r from-primary/10
                                  to-primary/5 -mx-6 px-6 pt-6">
            <div className="flex items-center gap-3">
              <div className="h-10 w-10 rounded-lg bg-primary/20 flex items-center justify-center
                              transition-transform hover:scale-105">
                <FileText className="h-5 w-5 text-primary" />
              </div>
              <div>
                <DialogTitle className="text-lg">申请详情</DialogTitle>
                <DialogDescription className="text-xs text-muted-foreground">
                  {selectedApplication ? `申请编号：${selectedApplication.applicationNo}` : '查看申请全量字段与合同信息'}
                </DialogDescription>
              </div>
            </div>
          </DialogHeader>

          {detailLoading ? (
            <div className="py-8 text-center">详情加载中...</div>
          ) : detailError ? (
            <div className="space-y-4 py-8 text-center">
              <p className="text-destructive">详情加载失败，请重试</p>
              <Button variant="outline" onClick={() => mutateDetail()}>
                重试
              </Button>
            </div>
          ) : !detail ? (
            <div className="py-8 text-center">暂无详情</div>
          ) : (
            <div className="space-y-6">
              <section className="rounded-xl border border-border/50 bg-card/50 p-4 shadow-sm
                      transition-all duration-200 hover:shadow-md hover:border-primary/20">
                <h3 className="text-sm font-semibold text-primary mb-3 flex items-center gap-2">
                  <div className="h-1.5 w-1.5 rounded-full bg-primary" />
                  基本信息
                </h3>
                <div className="grid grid-cols-2 gap-4">
                  <DetailField label="申请ID" value={detail.applicationId} />
                  <DetailField label="申请编号" value={detail.applicationNo} />
                  <DetailField label="客户ID" value={detail.customerId} />
                  <div className="space-y-1">
                    <Label className="text-muted-foreground">申请状态</Label>
                    <div>{renderEnumBadge(detail.status, APPLICATION_STATUS_MAP)}</div>
                  </div>
                  <DetailField label="客户姓名" value={detail.fullName} />
                  <DetailField label="HKID" value={detail.hkid} />
                  <DetailField label="家庭住址" value={detail.homeAddress} />
                  <DetailField label="年龄" value={detail.age} />
                  <DetailField label="产品编码" value={detail.productCode} />
                </div>
              </section>

              <section className="rounded-xl border border-border/50 bg-card/50 p-4 shadow-sm
                      transition-all duration-200 hover:shadow-md hover:border-primary/20">
                <h3 className="text-sm font-semibold text-primary mb-3 flex items-center gap-2">
                  <div className="h-1.5 w-1.5 rounded-full bg-primary" />
                  风险与财务
                </h3>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <Label className="text-muted-foreground">风控决策</Label>
                    <div>{renderEnumBadge(detail.riskDecision, RISK_DECISION_MAP)}</div>
                  </div>
                  <DetailField label="风控参考ID" value={detail.riskReferenceId} />
                  <DetailField label="核准金额" value={formatAmount(detail.approvedAmount)} />
                  <DetailField label="拒绝原因" value={detail.rejectReason} />
                  <DetailField label="职业" value={detail.occupation} />
                  <DetailField label="月收入" value={formatAmount(detail.monthlyIncome)} />
                  <DetailField label="月负债" value={formatAmount(detail.monthlyDebtPayment)} />
                </div>
              </section>

              <section className="rounded-xl border border-border/50 bg-card/50 p-4 shadow-sm
                      transition-all duration-200 hover:shadow-md hover:border-primary/20">
                <h3 className="text-sm font-semibold text-primary mb-3 flex items-center gap-2">
                  <div className="h-1.5 w-1.5 rounded-full bg-primary" />
                  时间轴
                </h3>
                <div className="flex items-center gap-2 overflow-x-auto pb-2">
                  {timelineItems.map((item, index) => (
                    <Fragment key={index}>
                      <div className="flex items-center gap-2 shrink-0">
                        <div className={cn(
                          "h-8 w-8 rounded-full flex items-center justify-center text-xs font-medium transition-all duration-300",
                          item.completed
                            ? "bg-primary text-primary-foreground"
                            : "bg-muted text-muted-foreground"
                        )}>
                          {item.completed ? <Check className="h-4 w-4" /> : (index + 1)}
                        </div>
                        <span className={cn(
                          "text-xs font-medium whitespace-nowrap",
                          item.completed ? "text-foreground" : "text-muted-foreground"
                        )}>
                          {item.label}
                        </span>
                      </div>
                      {index < timelineItems.length - 1 && (
                        <div className={cn(
                          "w-8 h-0.5 transition-all duration-300",
                          item.completed ? "bg-primary" : "bg-border"
                        )} />
                      )}
                    </Fragment>
                  ))}
                </div>
              </section>

              <section className="rounded-xl border border-border/50 bg-card/50 p-4 shadow-sm
                      transition-all duration-200 hover:shadow-md hover:border-primary/20">
                <h3 className="text-sm font-semibold text-primary mb-3 flex items-center gap-2">
                  <div className="h-1.5 w-1.5 rounded-full bg-primary" />
                  合同信息
                </h3>
                <div className="grid grid-cols-2 gap-4">
                  <DetailField label="合同号" value={detail.contractNo} />
                  <div className="space-y-1">
                    <Label className="text-muted-foreground">合同状态</Label>
                    <div>{renderEnumBadge(detail.contractStatus, CONTRACT_STATUS_MAP)}</div>
                  </div>
                  <DetailField label="模板版本" value={detail.templateVersion} />
                  <DetailField label="合同摘要" value={detail.digest} />
                  <DetailField label="合同签署时间" value={formatDateTime(detail.contractSignedAt)} />
                </div>
                <div className="space-y-2">
                  <Label className="text-muted-foreground">合同正文</Label>
                  <pre className="rounded-md bg-muted p-3 text-xs whitespace-pre-wrap break-words">
                    {textOrDash(detail.contractContent)}
                  </pre>
                </div>
              </section>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
