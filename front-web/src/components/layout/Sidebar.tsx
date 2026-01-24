'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useTranslations } from 'next-intl';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { useLayoutStore } from '@/lib/stores';
import { useMenus } from '@/lib/hooks/use-api';
import { useEffect } from 'react';
import { fetchMenus } from '@/lib/api/menu';

interface MenuItem {
  id: string;
  name: string;
  path: string;
  children?: MenuItem[];
}

interface SidebarItemProps {
  item: MenuItem;
  collapsed: boolean;
}

function SidebarItem({ item, collapsed }: SidebarItemProps) {
  const pathname = usePathname();
  const isActive = pathname === item.path || pathname?.startsWith(item.path + '/');

  return (
    <Link
      href={item.path}
      className={`
        flex items-center gap-3 rounded-lg px-3 py-2 transition-colors
        ${isActive ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'}
        ${collapsed ? 'justify-center' : ''}
      `}
      title={collapsed ? item.name : undefined}
    >
      {!collapsed && <span>{item.name}</span>}
    </Link>
  );
}

export function Sidebar() {
  const t = useTranslations();
  const { sidebarCollapsed, toggleSidebar } = useLayoutStore();
  const { data: menus, mutate } = useMenus();

  useEffect(() => {
    // 加载菜单数据
    fetchMenus().then((data) => {
      // mutate(data); // SWR 会自动缓存
    }).catch((err) => {
      console.error('Failed to fetch menus:', err);
    });
  }, []);

  return (
    <aside
      className={`
        flex flex-col border-r bg-card transition-all duration-300
        ${sidebarCollapsed ? 'w-16' : 'w-64'}
      `}
    >
      {/* Logo */}
      <div className="flex h-16 items-center justify-between border-b px-4">
        {!sidebarCollapsed && (
          <span className="text-lg font-bold">xWallet</span>
        )}
        <button
          onClick={toggleSidebar}
          className="rounded-md p-1 hover:bg-accent"
          aria-label={sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'}
        >
          {sidebarCollapsed ? <ChevronRight size={20} /> : <ChevronLeft size={20} />}
        </button>
      </div>

      {/* 菜单 */}
      <nav className="flex-1 space-y-1 overflow-y-auto p-2">
        {menus?.map((item) => (
          <SidebarItem key={item.id} item={item} collapsed={sidebarCollapsed} />
        ))}
      </nav>

      {/* 底部信息 */}
      <div className="border-t p-4">
        {!sidebarCollapsed && (
          <div className="text-xs text-muted-foreground">
            xWallet v1.0.0
          </div>
        )}
      </div>
    </aside>
  );
}
