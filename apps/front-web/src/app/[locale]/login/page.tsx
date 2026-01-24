'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useTranslations } from 'next-intl';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { login } from '@/lib/api/auth';
import { useAuthStore } from '@/lib/stores';

export default function LoginPage() {
  const t = useTranslations('auth');
  const tCommon = useTranslations('common');
  const router = useRouter();
  const { isAuthenticated } = useAuthStore();

  // 如果已登录，重定向到仪表盘
  if (isAuthenticated) {
    router.push('/zh-CN/dashboard');
    return null;
  }

  const [employeeNo, setEmployeeNo] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // 表单验证
    if (!employeeNo.trim()) {
      setError(t('pleaseInputEmployeeNo'));
      return;
    }
    if (!password.trim()) {
      setError(t('pleaseInputPassword'));
      return;
    }

    setLoading(true);

    try {
      await login({ employeeNo, password });
      // 登录成功，重定向到仪表盘
      router.push('/zh-CN/dashboard');
    } catch (err) {
      setError(err instanceof Error ? err.message : t('loginFailed'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <div className="w-full max-w-md space-y-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-foreground">
            {t('loginTitle')}
          </h1>
          <p className="mt-2 text-sm text-muted-foreground">
            {t('loginSubtitle')}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border p-6">
          {error && (
            <div className="rounded-md bg-destructive/15 p-3 text-sm text-destructive">
              {error}
            </div>
          )}

          <div className="space-y-2">
            <Label htmlFor="employeeNo">{t('employeeNo')}</Label>
            <Input
              id="employeeNo"
              type="text"
              value={employeeNo}
              onChange={(e) => setEmployeeNo(e.target.value)}
              placeholder={t('employeeNoPlaceholder')}
              disabled={loading}
              autoComplete="username"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">{t('password')}</Label>
            <Input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder={t('passwordPlaceholder')}
              disabled={loading}
              autoComplete="current-password"
            />
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? tCommon('loading') : t('login')}
          </Button>
        </form>

        <div className="text-center text-sm text-muted-foreground">
          <p>测试账号: ADMIN001 / admin123</p>
        </div>
      </div>
    </div>
  );
}
