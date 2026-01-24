'use client';

import { useTranslations } from 'next-intl';
import { useRouter, usePathname } from 'next/navigation';
import { useTheme } from 'next-themes';
import { LogOut, Moon, Sun, Globe } from 'lucide-react';
import { Button } from '@/components/ui/button';
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
  const { theme, setTheme } = useTheme();
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
    <header className="flex h-16 items-center justify-between border-b bg-card px-6">
      <div className="flex-1">
        {/* 左侧可以放置面包屑或其他内容 */}
      </div>

      <div className="flex items-center gap-2">
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
        <Button
          variant="ghost"
          size="icon"
          onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
        >
          <Sun className="h-5 w-5 rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
          <Moon className="absolute h-5 w-5 rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
          <span className="sr-only">切换主题</span>
        </Button>

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
