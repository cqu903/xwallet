'use client';

import Link from 'next/link';
import { usePathname, useParams } from 'next/navigation';
import { useTranslations } from 'next-intl';
import { ChevronLeft, ChevronRight, FolderOpen } from 'lucide-react';
import { useLayoutStore } from '@/lib/stores';
import { useMenus } from '@/lib/hooks/use-api';

interface MenuItem {
  id: string;
  name: string;
  path?: string | null;
  children?: MenuItem[];
}

interface SidebarItemProps {
  item: MenuItem;
  collapsed: boolean;
  locale: string;
  depth?: number;
}

function SidebarItem({ item, collapsed, locale, depth = 0 }: SidebarItemProps) {
  const pathname = usePathname();
  const hasChildren = item.children && item.children.length > 0;
  const path = item.path ?? null;

  // 有子菜单：渲染为目录（父级标题 + 缩进的子项）
  if (hasChildren) {
    return (
      <div className="space-y-0.5">
        <div
          className={`
            flex items-center gap-2 rounded-lg px-3 py-2
            text-muted-foreground text-sm font-medium
            ${collapsed ? 'justify-center px-2' : ''}
          `}
          title={item.name}
        >
          <FolderOpen className="h-4 w-4 shrink-0" />
          {!collapsed && <span>{item.name}</span>}
        </div>
        {!collapsed && (
          <div className={depth === 0 ? 'pl-4 space-y-0.5 border-l border-border/60 ml-3' : 'pl-2 space-y-0.5'}>
            {item.children!.map((child) => (
              <SidebarItem
                key={child.id}
                item={child}
                collapsed={collapsed}
                locale={locale}
                depth={depth + 1}
              />
            ))}
          </div>
        )}
      </div>
    );
  }

  // 叶子：可点击的 Link
  const href = path ? `/${locale}${path.startsWith('/') ? path : '/' + path}` : '#';
  const isActive = !!path && (pathname === href || pathname?.startsWith(href + '/'));

  return (
    <Link
      href={href}
      className={`
        flex items-center gap-3 rounded-lg px-3 py-2 transition-colors text-sm
        ${isActive ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'}
        ${collapsed ? 'justify-center px-2' : ''}
      `}
      title={collapsed ? item.name : undefined}
    >
      {!collapsed && <span>{item.name}</span>}
    </Link>
  );
}

export function Sidebar() {
  const t = useTranslations();
  const params = useParams();
  const locale = (params?.locale as string) || 'zh-CN';
  const { sidebarCollapsed, toggleSidebar } = useLayoutStore();
  const { data: menus } = useMenus();

  return (
    <aside
      className={`
        flex flex-col border-r border-border bg-card transition-all duration-300
        ${sidebarCollapsed ? 'w-16' : 'w-64'}
      `}
    >
      {/* Logo */}
      <div className="flex h-14 items-center justify-between border-b border-border px-3">
        {sidebarCollapsed ? (
          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-primary">
            <svg className="h-4 w-4 text-primary-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
        ) : (
          <div className="flex items-center gap-2">
            <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-primary">
              <svg className="h-4 w-4 text-primary-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <span className="font-display text-base font-semibold tracking-tight text-foreground">xWallet</span>
          </div>
        )}
        <button
          onClick={toggleSidebar}
          className="rounded-md p-1.5 text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
          aria-label={sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'}
        >
          {sidebarCollapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
        </button>
      </div>

      {/* 菜单 */}
      <nav className="flex-1 space-y-1 overflow-y-auto p-2">
        {menus?.map((item) => (
          <SidebarItem key={item.id} item={item} collapsed={sidebarCollapsed} locale={locale} />
        ))}
      </nav>

      {/* 底部信息 */}
      <div className="border-t border-border p-3">
        {!sidebarCollapsed && (
          <div className="text-xs text-muted-foreground">
            xWallet v1.0.0
          </div>
        )}
      </div>
    </aside>
  );
}
