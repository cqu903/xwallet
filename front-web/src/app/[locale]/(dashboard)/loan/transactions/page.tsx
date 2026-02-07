'use client';

import { useEffect, useMemo, useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
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
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  createAdminLoanTransaction,
  fetchAdminLoanTransactions,
  reverseAdminLoanTransaction,
  updateAdminLoanTransactionNote,
  type LoanTransactionAdminItem,
} from '@/lib/api/loan-transactions-admin';
import useSWR from 'swr';
import { FileText, Plus, Search } from 'lucide-react';

const PAGE_SIZE = 10;

const TRANSACTION_TYPES = [
  { value: 'INITIAL_DISBURSEMENT', label: '首放' },
  { value: 'REDRAW_DISBURSEMENT', label: '再提款' },
  { value: 'REPAYMENT', label: '还款' },
  { value: 'REVERSAL', label: '冲正' },
];

const STATUS_OPTIONS = [
  { value: 'POSTED', label: '已入账' },
  { value: 'REVERSED', label: '已冲正' },
];

const SOURCE_OPTIONS = [
  { value: 'APP', label: 'APP' },
  { value: 'ADMIN', label: 'ADMIN' },
  { value: 'SYSTEM', label: 'SYSTEM' },
];

export default function LoanTransactionsPage() {
  const [page, setPage] = useState(1);
  const [filters, setFilters] = useState({
    customerEmail: '',
    contractNo: '',
    type: '',
    status: '',
    source: '',
    idempotencyKey: '',
    createdBy: '',
    noteKeyword: '',
    startTime: '',
    endTime: '',
    amountMin: '',
    amountMax: '',
  });
  const [searchInput, setSearchInput] = useState({ ...filters });
  const [selectedTransaction, setSelectedTransaction] = useState<LoanTransactionAdminItem | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isReverseOpen, setIsReverseOpen] = useState(false);
  const [noteDraft, setNoteDraft] = useState('');
  const [reverseNote, setReverseNote] = useState('');
  const [createForm, setCreateForm] = useState({
    customerEmail: '',
    contractNo: '',
    type: 'REPAYMENT',
    amount: '',
    idempotencyKey: '',
    note: '',
  });

  const queryParams = useMemo(() => {
    return {
      page,
      size: PAGE_SIZE,
      customerEmail: filters.customerEmail || undefined,
      contractNo: filters.contractNo || undefined,
      type: filters.type || undefined,
      status: filters.status || undefined,
      source: filters.source || undefined,
      idempotencyKey: filters.idempotencyKey || undefined,
      createdBy: filters.createdBy || undefined,
      noteKeyword: filters.noteKeyword || undefined,
      startTime: filters.startTime || undefined,
      endTime: filters.endTime || undefined,
      amountMin: filters.amountMin || undefined,
      amountMax: filters.amountMax || undefined,
    };
  }, [filters, page]);

  const { data: transactionsData, isLoading, error, mutate } = useSWR(
    ['admin-loan-transactions', queryParams],
    () => fetchAdminLoanTransactions(queryParams),
    { revalidateOnFocus: false, shouldRetryOnError: false }
  );

  useEffect(() => {
    mutate();
  }, []);

  const handleSearch = () => {
    setFilters({ ...searchInput });
    setPage(1);
  };

  const handleReset = () => {
    const empty = {
      customerEmail: '',
      contractNo: '',
      type: '',
      status: '',
      source: '',
      idempotencyKey: '',
      createdBy: '',
      noteKeyword: '',
      startTime: '',
      endTime: '',
      amountMin: '',
      amountMax: '',
    };
    setSearchInput(empty);
    setFilters(empty);
    setPage(1);
  };

  const handleOpenDetail = (txn: LoanTransactionAdminItem) => {
    setSelectedTransaction(txn);
    setNoteDraft(txn.note || '');
    setIsDetailOpen(true);
  };

  const handleUpdateNote = async () => {
    if (!selectedTransaction) return;
    try {
      await updateAdminLoanTransactionNote(selectedTransaction.transactionId, noteDraft);
      await mutate();
      alert('备注更新成功');
    } catch (error) {
      console.error('更新备注失败:', error);
      alert(error instanceof Error ? error.message : '更新备注失败');
    }
  };

  const handleCreateTransaction = async () => {
    if (!createForm.customerEmail || !createForm.contractNo || !createForm.amount || !createForm.idempotencyKey) {
      alert('请填写完整必填项');
      return;
    }
    try {
      await createAdminLoanTransaction({
        customerEmail: createForm.customerEmail,
        contractNo: createForm.contractNo,
        type: createForm.type,
        amount: createForm.amount,
        idempotencyKey: createForm.idempotencyKey,
        note: createForm.note || undefined,
      });
      setIsCreateOpen(false);
      setCreateForm({
        customerEmail: '',
        contractNo: '',
        type: 'REPAYMENT',
        amount: '',
        idempotencyKey: '',
        note: '',
      });
      await mutate();
      alert('交易创建成功');
    } catch (error) {
      console.error('创建交易失败:', error);
      alert(error instanceof Error ? error.message : '创建交易失败');
    }
  };

  const handleReverse = async () => {
    if (!selectedTransaction) return;
    try {
      await reverseAdminLoanTransaction(selectedTransaction.transactionId, reverseNote || undefined);
      setIsReverseOpen(false);
      setReverseNote('');
      await mutate();
      alert('冲正成功');
    } catch (error) {
      console.error('冲正失败:', error);
      alert(error instanceof Error ? error.message : '冲正失败');
    }
  };

  const canReverse = (txn: LoanTransactionAdminItem) =>
    txn.status !== 'REVERSED' && txn.type !== 'REVERSAL' && txn.type !== 'INITIAL_DISBURSEMENT';

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="w-5 h-5" />
            交易记录管理
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <div className="space-y-2">
              <Label htmlFor="customerEmail">客户邮箱</Label>
              <Input
                id="customerEmail"
                placeholder="输入客户邮箱"
                value={searchInput.customerEmail}
                onChange={(e) => setSearchInput({ ...searchInput, customerEmail: e.target.value })}
              />
            </div>
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
              <Label htmlFor="type">交易类型</Label>
              <Select
                value={searchInput.type || 'all'}
                onValueChange={(value) => setSearchInput({ ...searchInput, type: value === 'all' ? '' : value })}
              >
                <SelectTrigger id="type">
                  <SelectValue placeholder="选择类型" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部</SelectItem>
                  {TRANSACTION_TYPES.map((item) => (
                    <SelectItem key={item.value} value={item.value}>
                      {item.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="status">交易状态</Label>
              <Select
                value={searchInput.status || 'all'}
                onValueChange={(value) => setSearchInput({ ...searchInput, status: value === 'all' ? '' : value })}
              >
                <SelectTrigger id="status">
                  <SelectValue placeholder="选择状态" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部</SelectItem>
                  {STATUS_OPTIONS.map((item) => (
                    <SelectItem key={item.value} value={item.value}>
                      {item.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="source">来源</Label>
              <Select
                value={searchInput.source || 'all'}
                onValueChange={(value) => setSearchInput({ ...searchInput, source: value === 'all' ? '' : value })}
              >
                <SelectTrigger id="source">
                  <SelectValue placeholder="选择来源" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部</SelectItem>
                  {SOURCE_OPTIONS.map((item) => (
                    <SelectItem key={item.value} value={item.value}>
                      {item.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="idempotencyKey">幂等键</Label>
              <Input
                id="idempotencyKey"
                placeholder="输入幂等键"
                value={searchInput.idempotencyKey}
                onChange={(e) => setSearchInput({ ...searchInput, idempotencyKey: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="createdBy">创建人</Label>
              <Input
                id="createdBy"
                placeholder="输入创建人"
                value={searchInput.createdBy}
                onChange={(e) => setSearchInput({ ...searchInput, createdBy: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="noteKeyword">备注关键词</Label>
              <Input
                id="noteKeyword"
                placeholder="输入备注关键词"
                value={searchInput.noteKeyword}
                onChange={(e) => setSearchInput({ ...searchInput, noteKeyword: e.target.value })}
              />
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
            <div className="space-y-2">
              <Label htmlFor="amountMin">金额下限</Label>
              <Input
                id="amountMin"
                placeholder="输入最小金额"
                value={searchInput.amountMin}
                onChange={(e) => setSearchInput({ ...searchInput, amountMin: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="amountMax">金额上限</Label>
              <Input
                id="amountMax"
                placeholder="输入最大金额"
                value={searchInput.amountMax}
                onChange={(e) => setSearchInput({ ...searchInput, amountMax: e.target.value })}
              />
            </div>
          </div>

          <div className="flex gap-2 mb-4">
            <Button onClick={handleSearch} className="gap-2">
              <Search className="w-4 h-4" />
              搜索
            </Button>
            <Button onClick={handleReset} variant="outline">
              重置
            </Button>
            <Button onClick={() => setIsCreateOpen(true)} className="ml-auto gap-2">
              <Plus className="w-4 h-4" />
              新增交易
            </Button>
          </div>

          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-[140px]">交易ID</TableHead>
                  <TableHead>类型</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead>发生时间</TableHead>
                  <TableHead>金额</TableHead>
                  <TableHead>客户邮箱</TableHead>
                  <TableHead>合同号</TableHead>
                  <TableHead>来源</TableHead>
                  <TableHead className="text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={9} className="text-center py-8">
                      加载中...
                    </TableCell>
                  </TableRow>
                ) : error ? (
                  <TableRow>
                    <TableCell colSpan={9} className="text-center py-8 text-destructive">
                      加载失败，请重试
                    </TableCell>
                  </TableRow>
                ) : transactionsData?.list?.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={9} className="text-center py-8">
                      暂无交易记录
                    </TableCell>
                  </TableRow>
                ) : (
                  transactionsData?.list?.map((txn) => (
                    <TableRow key={txn.transactionId}>
                      <TableCell className="font-mono text-xs">{txn.transactionId}</TableCell>
                      <TableCell>{txn.type}</TableCell>
                      <TableCell>{txn.status}</TableCell>
                      <TableCell>{txn.occurredAt}</TableCell>
                      <TableCell>{txn.amount}</TableCell>
                      <TableCell>{txn.customerEmail}</TableCell>
                      <TableCell>{txn.contractId}</TableCell>
                      <TableCell>{txn.source}</TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                          <Button variant="ghost" size="sm" onClick={() => handleOpenDetail(txn)}>
                            详情
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            disabled={!canReverse(txn)}
                            onClick={() => {
                              setSelectedTransaction(txn);
                              setIsReverseOpen(true);
                            }}
                          >
                            冲正
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>

          {transactionsData && transactionsData.total > 0 && (
            <div className="flex items-center justify-between mt-4">
              <div className="text-sm text-muted-foreground">
                共 {transactionsData.total} 条记录，第 {page} / {Math.ceil(transactionsData.total / transactionsData.size)} 页
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
                  disabled={page * transactionsData.size >= transactionsData.total}
                >
                  下一页
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <Dialog open={isDetailOpen} onOpenChange={setIsDetailOpen}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>交易详情</DialogTitle>
            <DialogDescription>查看交易摘要与清分信息</DialogDescription>
          </DialogHeader>
          {selectedTransaction && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-muted-foreground">交易号</Label>
                  <p className="font-mono text-sm">{selectedTransaction.transactionId}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">交易类型</Label>
                  <p>{selectedTransaction.type}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">交易状态</Label>
                  <p>{selectedTransaction.status}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">发生时间</Label>
                  <p>{selectedTransaction.occurredAt}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">金额</Label>
                  <p>{selectedTransaction.amount}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">来源</Label>
                  <p>{selectedTransaction.source}</p>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-muted-foreground">本金拆分</Label>
                  <p>{selectedTransaction.principalComponent || '-'}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">利息拆分</Label>
                  <p>{selectedTransaction.interestComponent || '-'}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">客户邮箱</Label>
                  <p>{selectedTransaction.customerEmail}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">合同号</Label>
                  <p>{selectedTransaction.contractId}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">幂等键</Label>
                  <p className="font-mono text-sm">{selectedTransaction.idempotencyKey}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">创建人</Label>
                  <p>{selectedTransaction.createdBy || '-'}</p>
                </div>
              </div>

              <div>
                <Label className="text-muted-foreground">备注</Label>
                <Textarea
                  value={noteDraft}
                  onChange={(e) => setNoteDraft(e.target.value)}
                  placeholder="填写备注"
                />
              </div>
            </div>
          )}
          <DialogFooter className="gap-3">
            <Button variant="outline" onClick={() => setIsDetailOpen(false)}>
              关闭
            </Button>
            <Button onClick={handleUpdateNote}>保存备注</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>新增交易</DialogTitle>
            <DialogDescription>仅支持还款与再提款，发生时间为当前时间</DialogDescription>
          </DialogHeader>
          <div className="grid grid-cols-1 gap-3">
            <div className="space-y-2">
              <Label htmlFor="createCustomerEmail">客户邮箱</Label>
              <Input
                id="createCustomerEmail"
                value={createForm.customerEmail}
                onChange={(e) => setCreateForm({ ...createForm, customerEmail: e.target.value })}
                placeholder="输入客户邮箱"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="createContractNo">合同号</Label>
              <Input
                id="createContractNo"
                value={createForm.contractNo}
                onChange={(e) => setCreateForm({ ...createForm, contractNo: e.target.value })}
                placeholder="输入合同号"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="createType">交易类型</Label>
              <Select
                value={createForm.type}
                onValueChange={(value) => setCreateForm({ ...createForm, type: value })}
              >
                <SelectTrigger id="createType">
                  <SelectValue placeholder="选择类型" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="REPAYMENT">还款</SelectItem>
                  <SelectItem value="REDRAW_DISBURSEMENT">再提款</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="createAmount">金额</Label>
              <Input
                id="createAmount"
                value={createForm.amount}
                onChange={(e) => setCreateForm({ ...createForm, amount: e.target.value })}
                placeholder="输入金额"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="createIdempotencyKey">幂等键</Label>
              <Input
                id="createIdempotencyKey"
                value={createForm.idempotencyKey}
                onChange={(e) => setCreateForm({ ...createForm, idempotencyKey: e.target.value })}
                placeholder="输入幂等键"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="createNote">备注</Label>
              <Textarea
                id="createNote"
                value={createForm.note}
                onChange={(e) => setCreateForm({ ...createForm, note: e.target.value })}
                placeholder="填写备注（可选）"
              />
            </div>
          </div>
          <DialogFooter className="gap-3">
            <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
              取消
            </Button>
            <Button onClick={handleCreateTransaction}>提交</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isReverseOpen} onOpenChange={setIsReverseOpen}>
        <DialogContent className="sm:max-w-[420px]">
          <DialogHeader>
            <DialogTitle>冲正交易</DialogTitle>
            <DialogDescription>确认后将生成冲正流水，原交易标记为已冲正</DialogDescription>
          </DialogHeader>
          <div className="space-y-2">
            <Label htmlFor="reverseNote">备注</Label>
            <Textarea
              id="reverseNote"
              value={reverseNote}
              onChange={(e) => setReverseNote(e.target.value)}
              placeholder="填写冲正原因（可选）"
            />
          </div>
          <DialogFooter className="gap-3">
            <Button variant="outline" onClick={() => setIsReverseOpen(false)}>
              取消
            </Button>
            <Button onClick={handleReverse}>确认冲正</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
