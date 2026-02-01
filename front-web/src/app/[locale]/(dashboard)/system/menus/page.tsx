'use client';

import { useState } from 'react';
import { useTranslations } from 'next-intl';
import { FolderOpen, Plus, Edit, Trash2, CheckCircle, XCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
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
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { useApi } from '@/lib/hooks/use-api';

/** 菜单树节点（后端 MenuItemDTO：id 为 string，可能有 children） */
interface MenuItem {
  id: string;
  name: string;
  path: string | null;
  children?: MenuItem[];
}

/** 后端 ResponseResult 的 data 结构 */
interface MenuListResponse {
  data?: MenuItem[];
}

export default function MenusPage() {
  const t = useTranslations();
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingMenu, setEditingMenu] = useState<MenuItem | null>(null);

  // 获取菜单列表（后端返回 ResponseResult{ data }，需解包）
  const { data: res, isLoading, error } = useApi<MenuListResponse | null>('/menus');
  const menus = res?.data ?? null;

  // 扁平化菜单树，便于表格展示（暂不使用，直接在渲染时递归）
  // const flattenMenus = (menuList: MenuItem[]): MenuItem[] => {
  //   const result: MenuItem[] = [];
  //   const flatten = (items: MenuItem[], level = 0) => {
  //     if (!items) return;
  //     items.forEach((item) => {
  //       result.push({ ...item, level });
  //       if (item.children && item.children.length > 0) {
  //         flatten(item.children, level + 1);
  //       }
  //     });
  //   };
  //   flatten(menuList);
  //   return result;
  // };

  const handleOpenCreateDialog = () => {
    setEditingMenu(null);
    setIsDialogOpen(true);
  };

  const handleOpenEditDialog = (menu: MenuItem) => {
    setEditingMenu(menu);
    setIsDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setIsDialogOpen(false);
    setEditingMenu(null);
  };

  // 加载状态
  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center space-y-2">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-primary border-r-transparent" />
          <p className="text-sm text-muted-foreground">加载中...</p>
        </div>
      </div>
    );
  }

  // 错误状态
  if (error || !menus) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center space-y-2">
          <XCircle className="h-12 w-12 text-destructive mx-auto" />
          <p className="text-sm text-muted-foreground">加载菜单失败</p>
        </div>
      </div>
    );
  }

  // 空状态
  if (menus.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center space-y-2">
          <FolderOpen className="h-12 w-12 text-muted-foreground mx-auto" />
          <p className="text-sm text-muted-foreground">暂无菜单数据</p>
        </div>
      </div>
    );
  }

  // 渲染菜单行（递归）
  const renderMenuRow = (menu: MenuItem, level = 0): React.ReactNode[] => {
    const rows: React.ReactNode[] = [];
    const indent = level * 24;

    rows.push(
      <TableRow key={menu.id}>
        <TableCell>
          <div style={{ paddingLeft: `${indent}px` }} className="flex items-center gap-2">
            {menu.children && menu.children.length > 0 ? (
              <FolderOpen className="h-4 w-4 text-primary" />
            ) : null}
            <span className="font-medium">{menu.name}</span>
          </div>
        </TableCell>
        <TableCell>
          {menu.path ? (
            <Badge variant="secondary">{menu.path}</Badge>
          ) : (
            <span className="text-muted-foreground">-</span>
          )}
        </TableCell>
        <TableCell>
          <div className="flex items-center gap-2">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => handleOpenEditDialog(menu)}
              className="h-8 w-8"
            >
              <Edit className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8 text-destructive hover:text-destructive"
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          </div>
        </TableCell>
      </TableRow>
    );

    // 递归渲染子菜单
    if (menu.children && menu.children.length > 0) {
      menu.children.forEach((child) => {
        rows.push(...renderMenuRow(child, level + 1));
      });
    }

    return rows;
  };

  return (
    <div className="space-y-8 animate-fade-in">
      {/* 欢迎横幅 - 紫色渐变 */}
      <div className="relative overflow-hidden rounded-xl border border-border bg-card shadow-sm">
        <div className="absolute inset-0 gradient-bg opacity-95" />
        <div className="relative p-6">
          <div className="flex items-center justify-between">
            <div className="space-y-2">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-white/20 backdrop-blur-sm">
                  <FolderOpen className="h-5 w-5 text-white" />
                </div>
                <div>
                  <h1 className="font-display text-2xl font-bold text-white">菜单管理</h1>
                  <p className="text-sm text-white/90">
                    管理系统菜单和权限结构
                  </p>
                </div>
              </div>
            </div>
            <Button
              onClick={handleOpenCreateDialog}
              className="h-11 bg-white text-primary hover:bg-white/90 shadow-sm font-medium"
            >
              <Plus className="mr-2 h-4 w-4" />
              新增菜单
            </Button>
          </div>
        </div>
      </div>

      {/* 菜单列表表格 */}
      <Card className="border border-border shadow-sm">
        <CardHeader>
          <CardTitle className="text-lg font-semibold">菜单列表</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>菜单名称</TableHead>
                <TableHead>路径</TableHead>
                <TableHead>操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {menus.map((menu) => renderMenuRow(menu))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* 新增/编辑菜单对话框 */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingMenu ? '编辑菜单' : '新增菜单'}</DialogTitle>
            <DialogDescription>
              {editingMenu ? '修改菜单信息' : '创建新的菜单项'}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="menu-name">菜单名称</Label>
              <Input
                id="menu-name"
                placeholder="请输入菜单名称"
                defaultValue={editingMenu?.name}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="menu-path">菜单路径</Label>
              <Input
                id="menu-path"
                placeholder="请输入菜单路径（可选）"
                defaultValue={editingMenu?.path ?? ''}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={handleCloseDialog}>
              取消
            </Button>
            <Button>保存</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
