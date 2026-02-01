'use client';

import { useTranslations } from 'next-intl';
import { useRouter, usePathname } from 'next/navigation';
import { LogOut, Globe } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ThemeToggle } from '@/components/ui/theme-toggle';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { useAuthStore } from '@/lib/stores';
import { logout } from '@/lib/api/auth';

export function Header() {
  const t = useTranslations();
  const router = useRouter();
  const pathname = usePathname();
  const { user } = useAuthStore();

  const handleLogout = () => {
    logout();
    router.push('/zh-CN/login');
  };

  const switchLanguage = (newLocale: string) => {
    // 替换路径中的语言部分
    const newPath = pathname?.replace(/^\/[^\/]+/, `/${newLocale}`);
    router.push(newPath || `/${newLocale}`);
  };

  const currentLocale = pathname?.split('/')[1] || 'zh-CN';

  return (
    <header className="flex h-14 items-center justify-between border-b border-border bg-card px-6 shadow-sm">
      <div className="flex-1" />

      <div className="flex items-center gap-1">
        {/* 语言切换 */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <Globe className="h-5 w-5" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => switchLanguage('zh-CN')}>
              简体中文
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => switchLanguage('en-US')}>
              English
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>

        {/* 主题切换 */}
        <ThemeToggle />

        {/* 用户信息 */}
        <div className="flex items-center gap-2">
          <span className="text-sm text-muted-foreground">
            {user?.username || 'Admin'}
          </span>
          <Button variant="ghost" size="icon" onClick={handleLogout}>
            <LogOut className="h-5 w-5" />
            <span className="sr-only">登出</span>
          </Button>
        </div>
      </div>
    </header>
  );
}
