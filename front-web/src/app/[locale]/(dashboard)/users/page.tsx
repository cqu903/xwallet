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
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { fetchUsers, createUser, updateUser, deleteUser, type User } from '@/lib/api/users';
import { RoleMultiSelect } from '@/components/users/RoleMultiSelect';
import { validateCreateUserForm, validateUpdateUserForm } from '@/lib/utils/validation';
import useSWR from 'swr';
import { Users, UserCheck, UserX, Search } from 'lucide-react';

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
  const [errors, setErrors] = useState<Record<string, string>>({});

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
    setErrors({});
  };

  const handleSubmit = async () => {
    // 清空之前的错误
    setErrors({});

    // 表单验证
    if (editingUser) {
      // 更新用户验证
      const validationResult = validateUpdateUserForm({
        username: formData.username,
        email: formData.email,
        roleIds: formData.roleIds,
      });

      if (!validationResult.valid) {
        setErrors(validationResult.errors);
        return;
      }
    } else {
      // 新增用户验证
      const validationResult = validateCreateUserForm({
        employeeNo: formData.employeeNo,
        username: formData.username,
        email: formData.email,
        password: formData.password,
        roleIds: formData.roleIds,
      });

      if (!validationResult.valid) {
        setErrors(validationResult.errors);
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
        alert('用户更新成功');
      } else {
        // 新增用户
        await createUser({
          employeeNo: formData.employeeNo,
          username: formData.username,
          email: formData.email,
          password: formData.password,
          roleIds: formData.roleIds,
        });
        alert('用户创建成功');
      }
      handleCloseDialog();
      mutate();
    } catch (error) {
      console.error('操作失败:', error);
      const errorMessage = error instanceof Error ? error.message : '操作失败';
      alert(errorMessage);
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

  const getStatusBadge = (status: number) => {
    if (status === 1) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-500/10 text-green-600 dark:text-green-400">
          启用
        </span>
      );
    }
    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-500/10 text-red-600 dark:text-red-400">
        禁用
      </span>
    );
  };

  // 计算统计数据
  const totalUsers = usersData?.totalElements || 0;
  const activeUsers = usersData?.content?.filter(u => u.status === 1).length || 0;
  const inactiveUsers = usersData?.content?.filter(u => u.status === 0).length || 0;

  return (
    <div className="space-y-8 animate-fade-in">
      {/* 页面标题 - 渐变横幅 */}
      <div className="relative overflow-hidden rounded-xl border border-border bg-card shadow-sm">
        <div className="absolute inset-0 gradient-bg opacity-95" />
        <div className="relative p-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="font-display text-3xl font-bold text-white mb-2">{t('user.title')}</h1>
              <p className="text-white/80">
                管理系统用户和权限
              </p>
            </div>
            <Button
              onClick={handleOpenCreateDialog}
              className="h-11 bg-white text-primary hover:bg-white/90 shadow-sm font-medium"
            >
              <Users className="mr-2 h-4 w-4" />
              {t('user.addUser')}
            </Button>
          </div>
        </div>
      </div>

      {/* 统计卡片 */}
      <div className="grid gap-6 md:grid-cols-3">
        <Card className="relative card-hover overflow-hidden border border-border shadow-sm isolate">
          <div className="absolute inset-0 bg-gradient-to-br from-primary/15 to-primary/25 opacity-50 -z-10" />
          <CardHeader className="relative">
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm font-medium text-muted-foreground">总用户数</CardTitle>
              <div className="rounded-lg bg-primary p-2 text-white shadow-md">
                <Users className="h-4 w-4" />
              </div>
            </div>
          </CardHeader>
          <CardContent className="relative">
            <div className="text-3xl font-bold text-gradient">{totalUsers}</div>
            <p className="text-xs text-muted-foreground mt-1">系统注册用户总数</p>
          </CardContent>
        </Card>

        <Card className="relative card-hover overflow-hidden border border-border shadow-sm isolate">
          <div className="absolute inset-0 bg-gradient-to-br from-green-500/15 to-green-600/25 opacity-50 -z-10" />
          <CardHeader className="relative">
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm font-medium text-muted-foreground">启用用户</CardTitle>
              <div className="rounded-lg bg-green-600 p-2 text-white shadow-md">
                <UserCheck className="h-4 w-4" />
              </div>
            </div>
          </CardHeader>
          <CardContent className="relative">
            <div className="text-3xl font-bold text-green-600 dark:text-green-400">{activeUsers}</div>
            <p className="text-xs text-muted-foreground mt-1">当前页启用用户数</p>
          </CardContent>
        </Card>

        <Card className="relative card-hover overflow-hidden border border-border shadow-sm isolate">
          <div className="absolute inset-0 bg-gradient-to-br from-red-500/15 to-red-600/25 opacity-50 -z-10" />
          <CardHeader className="relative">
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm font-medium text-muted-foreground">禁用用户</CardTitle>
              <div className="rounded-lg bg-red-600 p-2 text-white shadow-md">
                <UserX className="h-4 w-4" />
              </div>
            </div>
          </CardHeader>
          <CardContent className="relative">
            <div className="text-3xl font-bold text-red-600 dark:text-red-400">{inactiveUsers}</div>
            <p className="text-xs text-muted-foreground mt-1">当前页禁用用户数</p>
          </CardContent>
        </Card>
      </div>

      {/* 搜索栏 - 卡片样式 */}
      <Card className="border border-border shadow-sm">
        <CardContent className="pt-6">
          <div className="flex gap-3">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="搜索用户（工号/用户名/邮箱）"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                className="pl-10 h-11"
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              />
            </div>
            <Button onClick={handleSearch} className="h-11 shadow-sm hover:shadow transition-shadow">
              {t('common.search')}
            </Button>
            <Button variant="outline" onClick={handleReset} className="h-11">
              {t('common.reset')}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* 用户列表表格 */}
      <Card className="border border-border shadow-sm">
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
                <TableCell colSpan={6} className="text-center py-8">
                  <div className="flex items-center justify-center">
                    <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
                    <span className="ml-2 text-muted-foreground">{t('common.loading')}</span>
                  </div>
                </TableCell>
              </TableRow>
            ) : usersData?.content && usersData.content.length > 0 ? (
              usersData.content.map((user) => (
                <TableRow key={user.id} className="hover:bg-muted/50 transition-colors">
                  <TableCell className="font-medium">{user.employeeNo}</TableCell>
                  <TableCell>{user.username}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>
                    <div className="flex flex-wrap gap-1">
                      {user.roles?.map((role) => (
                        <span
                          key={role.id}
                          className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-primary/10 text-primary"
                        >
                          {role.roleName}
                        </span>
                      )) || '-'}
                    </div>
                  </TableCell>
                  <TableCell>{getStatusBadge(user.status)}</TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleOpenEditDialog(user)}
                      className="hover:bg-primary/10 hover:text-primary transition-colors"
                    >
                      {t('common.edit')}
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-destructive hover:bg-destructive/10 transition-colors"
                      onClick={() => handleDeleteClick(user)}
                    >
                      {t('common.delete')}
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-8">
                  <div className="flex flex-col items-center justify-center space-y-3">
                    <Users className="h-12 w-12 text-muted-foreground/30" />
                    <p className="text-muted-foreground">{t('common.noData')}</p>
                  </div>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Card>

      {/* 分页信息 */}
      {usersData && usersData.totalPages > 1 && (
        <Card className="border border-border shadow-sm">
          <CardContent className="py-4">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">
                共 <span className="font-medium text-foreground">{usersData.totalElements}</span> 条记录，
                第 <span className="font-medium text-foreground">{usersData.page}</span> / <span className="font-medium text-foreground">{usersData.totalPages}</span> 页
              </span>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage(p => Math.max(1, p - 1))}
                  disabled={page === 1}
                  className="h-9"
                >
                  上一页
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage(p => p + 1)}
                  disabled={page >= (usersData?.totalPages || 0)}
                  className="h-9"
                >
                  下一页
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* 新增/编辑对话框 */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle className="font-display text-xl">
              {editingUser ? t('user.editUser') : t('user.addUser')}
            </DialogTitle>
            <DialogDescription>
              {editingUser ? '编辑用户信息和角色权限' : '创建新用户账号并分配角色'}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-5 py-4">
            <div className="space-y-2">
              <Label htmlFor="employeeNo" className="text-sm font-medium">
                工号 <span className="text-destructive">*</span>
              </Label>
              <Input
                id="employeeNo"
                value={formData.employeeNo}
                onChange={(e) => setFormData({ ...formData, employeeNo: e.target.value.toUpperCase() })}
                disabled={!!editingUser}
                placeholder="如：ADMIN001"
                className={`h-11 ${errors.employeeNo ? 'border-destructive focus-visible:ring-destructive' : ''}`}
              />
              {errors.employeeNo && (
                <p className="text-xs text-destructive flex items-center">
                  <span className="mr-1">⚠</span> {errors.employeeNo}
                </p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="username" className="text-sm font-medium">
                用户名 <span className="text-destructive">*</span>
              </Label>
              <Input
                id="username"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                placeholder="请输入用户名"
                className={`h-11 ${errors.username ? 'border-destructive focus-visible:ring-destructive' : ''}`}
              />
              {errors.username && (
                <p className="text-xs text-destructive flex items-center">
                  <span className="mr-1">⚠</span> {errors.username}
                </p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="email" className="text-sm font-medium">
                邮箱 <span className="text-destructive">*</span>
              </Label>
              <Input
                id="email"
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                placeholder="example@company.com"
                className={`h-11 ${errors.email ? 'border-destructive focus-visible:ring-destructive' : ''}`}
              />
              {errors.email && (
                <p className="text-xs text-destructive flex items-center">
                  <span className="mr-1">⚠</span> {errors.email}
                </p>
              )}
            </div>
            {!editingUser && (
              <div className="space-y-2">
                <Label htmlFor="password" className="text-sm font-medium">
                  密码 <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="password"
                  type="password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  placeholder="6-20位字符"
                  className={`h-11 ${errors.password ? 'border-destructive focus-visible:ring-destructive' : ''}`}
                />
                {errors.password && (
                  <p className="text-xs text-destructive flex items-center">
                    <span className="mr-1">⚠</span> {errors.password}
                  </p>
                )}
              </div>
            )}
            <div className="space-y-2">
              <Label htmlFor="roles" className="text-sm font-medium">
                角色 <span className="text-destructive">*</span>
              </Label>
              <RoleMultiSelect
                selectedRoleIds={formData.roleIds}
                onChange={(roleIds) => {
                  setFormData({ ...formData, roleIds });
                  if (roleIds.length > 0) {
                    setErrors(prev => {
                      const newErrors = { ...prev };
                      delete newErrors.roleIds;
                      return newErrors;
                    });
                  }
                }}
              />
              {errors.roleIds && (
                <p className="text-xs text-destructive flex items-center">
                  <span className="mr-1">⚠</span> {errors.roleIds}
                </p>
              )}
              {formData.roleIds.length === 0 && !errors.roleIds && (
                <p className="text-xs text-muted-foreground">
                  请至少选择一个角色
                </p>
              )}
            </div>
          </div>
          <DialogFooter className="gap-3">
            <Button variant="outline" onClick={handleCloseDialog} className="h-11">
              {t('common.cancel')}
            </Button>
            <Button onClick={handleSubmit} className="h-11 shadow-sm hover:shadow transition-shadow">
              {t('common.save')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 删除确认对话框 */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent className="sm:max-w-[400px]">
          <DialogHeader>
            <DialogTitle className="font-display text-xl">确认删除用户</DialogTitle>
            <DialogDescription className="text-base">
              确定要删除该用户吗？此操作不可恢复。
            </DialogDescription>
          </DialogHeader>
          <DialogFooter className="gap-3">
            <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)} className="h-11">
              {t('common.cancel')}
            </Button>
            <Button variant="destructive" onClick={handleConfirmDelete} className="h-11">
              {t('common.delete')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
