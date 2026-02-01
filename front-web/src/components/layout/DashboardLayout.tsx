'use client';

import { ReactNode } from 'react';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { useAuthStore, useAuthHydration } from '@/lib/stores';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

interface DashboardLayoutProps {
  children: ReactNode;
}

export function DashboardLayout({ children }: DashboardLayoutProps) {
  const { isAuthenticated } = useAuthStore();
  const router = useRouter();
  const [isHydrated, setIsHydrated] = useState(false);

  // 在客户端首次渲染时触发 hydration
  useAuthHydration();

  useEffect(() => {
    // 标记组件已在客户端挂载
    setIsHydrated(true);
  }, []);

  useEffect(() => {
    // 只在客户端挂载后才执行重定向逻辑
    if (isHydrated && !isAuthenticated) {
      router.push('/zh-CN/login');
    }
  }, [isAuthenticated, router, isHydrated]);

  // SSR 或未登录时显示加载占位符，避免 hydration 不一致
  if (!isHydrated || !isAuthenticated) {
    return (
      <div className="flex h-screen items-center justify-center bg-background">
        <div className="text-muted-foreground">加载中...</div>
      </div>
    );
  }

  return (
    <div className="flex h-screen bg-background">
      {/* 侧边栏 */}
      <Sidebar />

      {/* 主内容区 */}
      <div className="flex flex-1 flex-col overflow-hidden">
        {/* 顶部栏 */}
        <Header />

        {/* 页面内容 */}
        <main className="flex-1 overflow-y-auto p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
