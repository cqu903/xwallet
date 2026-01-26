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
import { Label } from '@/components/ui/label';
import { fetchUsers, createUser, updateUser, deleteUser, type User, type CreateUserRequest, type UpdateUserRequest } from '@/lib/api/users';
import { RoleMultiSelect } from '@/components/users/RoleMultiSelect';
import useSWR from 'swr';

export default function UsersPage() {
  const t = useTranslations();
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [deletingUserId, setDeletingUserId] = useState<number | null>(null);
  const [formData, setFormData] = useState({
    employeeNo: '',
    username: '',
    email: '',
    password: '',
    roleIds: [] as number[],
  });

  // 获取用户列表（GET /user/list）
  const { data: usersData, isLoading, mutate } = useSWR(
    ['users', page, keyword],
    () => fetchUsers({ page, size: 10, keyword: keyword || undefined }),
    { revalidateOnFocus: false, shouldRetryOnError: false }
  );

  useEffect(() => {
    // 初始加载用户列表
    mutate();
  }, []);

  const handleSearch = () => {
    setKeyword(searchInput);
    setPage(1);
  };

  const handleReset = () => {
    setSearchInput('');
    setKeyword('');
    setPage(1);
  };

  const handleOpenCreateDialog = () => {
    setEditingUser(null);
    setFormData({
      employeeNo: '',
      username: '',
      email: '',
      password: '',
      roleIds: [],
    });
    setIsDialogOpen(true);
  };

  const handleOpenEditDialog = (user: User) => {
    setEditingUser(user);
    setFormData({
      employeeNo: user.employeeNo,
      username: user.username,
      email: user.email,
      password: '',
      roleIds: user.roles?.map(r => r.id) || [],
    });
    setIsDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setIsDialogOpen(false);
    setEditingUser(null);
    setFormData({
      employeeNo: '',
      username: '',
      email: '',
      password: '',
      roleIds: [],
    });
  };

  const handleSubmit = async () => {
    // 提交前验证：确保角色已选择
    if (formData.roleIds.length === 0) {
      alert('请至少选择一个角色');
      return;
    }

    // 提交前验证：新增时检查必填字段
    if (!editingUser) {
      if (!formData.employeeNo || !formData.username || !formData.email || !formData.password) {
        alert('请填写所有必填字段');
        return;
      }
    }

    try {
      if (editingUser) {
        // 更新用户
        await updateUser(editingUser.id, {
          username: formData.username,
          email: formData.email,
          roleIds: formData.roleIds,
        });
      } else {
        // 新增用户
        await createUser({
          employeeNo: formData.employeeNo,
          username: formData.username,
          email: formData.email,
          password: formData.password,
          roleIds: formData.roleIds,
        });
      }
      handleCloseDialog();
      mutate();
    } catch (error) {
      console.error('操作失败:', error);
      alert(error instanceof Error ? error.message : '操作失败');
    }
  };

  const handleDeleteClick = (user: User) => {
    setDeletingUserId(user.id);
    setIsDeleteDialogOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (deletingUserId) {
      try {
        await deleteUser(deletingUserId);
        setIsDeleteDialogOpen(false);
        setDeletingUserId(null);
        mutate();
      } catch (error) {
        console.error('删除失败:', error);
        alert(error instanceof Error ? error.message : '删除失败');
      }
    }
  };

  const getStatusText = (status: number) => {
    return status === 1 ? '启用' : '禁用';
  };

  const getStatusClass = (status: number) => {
    return status === 1 ? 'text-green-600' : 'text-red-600';
  };

  return (
    <div className="space-y-6">
      {/* 页面标题和操作按钮 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">{t('user.title')}</h1>
          <p className="text-muted-foreground">
            管理系统用户和权限
          </p>
        </div>
        <Button onClick={handleOpenCreateDialog}>
          {t('user.addUser')}
        </Button>
      </div>

      {/* 搜索栏 */}
      <div className="flex gap-2">
        <Input
          placeholder="搜索用户（工号/用户名/邮箱）"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          className="max-w-sm"
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
        />
        <Button onClick={handleSearch}>{t('common.search')}</Button>
        <Button variant="outline" onClick={handleReset}>
          {t('common.reset')}
        </Button>
      </div>

      {/* 用户列表表格 */}
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>工号</TableHead>
              <TableHead>用户名</TableHead>
              <TableHead>邮箱</TableHead>
              <TableHead>角色</TableHead>
              <TableHead>状态</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center">
                  {t('common.loading')}
                </TableCell>
              </TableRow>
            ) : usersData?.content && usersData.content.length > 0 ? (
              usersData.content.map((user) => (
                <TableRow key={user.id}>
                  <TableCell>{user.employeeNo}</TableCell>
                  <TableCell>{user.username}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>
                    {user.roles?.map((role) => role.roleName).join(', ') || '-'}
                  </TableCell>
                  <TableCell className={getStatusClass(user.status)}>
                    {getStatusText(user.status)}
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleOpenEditDialog(user)}
                    >
                      {t('common.edit')}
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-destructive"
                      onClick={() => handleDeleteClick(user)}
                    >
                      {t('common.delete')}
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={6} className="text-center text-muted-foreground">
                  {t('common.noData')}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/* 分页信息 */}
      {usersData && (
        <div className="flex items-center justify-between text-sm text-muted-foreground">
          <span>
            共 {usersData.totalElements} 条记录，第 {usersData.page} / {usersData.totalPages} 页
          </span>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage(p => Math.max(1, p - 1))}
              disabled={page === 1}
            >
              上一页
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage(p => p + 1)}
              disabled={page >= (usersData?.totalPages || 0)}
            >
              下一页
            </Button>
          </div>
        </div>
      )}

      {/* 新增/编辑对话框 */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>
              {editingUser ? t('user.editUser') : t('user.addUser')}
            </DialogTitle>
            <DialogDescription>
              {editingUser ? '编辑用户信息' : '创建新用户账号'}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="employeeNo">
                工号 <span className="text-destructive">*</span>
              </Label>
              <Input
                id="employeeNo"
                value={formData.employeeNo}
                onChange={(e) => setFormData({ ...formData, employeeNo: e.target.value })}
                disabled={!!editingUser}
                placeholder="请输入工号"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="username">
                用户名 <span className="text-destructive">*</span>
              </Label>
              <Input
                id="username"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                placeholder="请输入用户名"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="email">
                邮箱 <span className="text-destructive">*</span>
              </Label>
              <Input
                id="email"
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                placeholder="请输入邮箱"
              />
            </div>
            {!editingUser && (
              <div className="space-y-2">
                <Label htmlFor="password">
                  密码 <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="password"
                  type="password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  placeholder="请输入密码"
                />
              </div>
            )}
            <div className="space-y-2">
              <Label htmlFor="roles">
                角色 <span className="text-destructive">*</span>
              </Label>
              <RoleMultiSelect
                selectedRoleIds={formData.roleIds}
                onChange={(roleIds) => setFormData({ ...formData, roleIds })}
              />
              {formData.roleIds.length === 0 && (
                <p className="text-xs text-destructive">
                  请至少选择一个角色
                </p>
              )}
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={handleCloseDialog}>
              {t('common.cancel')}
            </Button>
            <Button onClick={handleSubmit}>
              {t('common.save')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 删除确认对话框 */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>确认删除</DialogTitle>
            <DialogDescription>
              {t('user.confirmDelete')}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button variant="destructive" onClick={handleConfirmDelete}>
              {t('common.delete')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
