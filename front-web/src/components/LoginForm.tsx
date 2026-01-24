'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useTranslations } from 'next-intl';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { login } from '@/lib/api/auth';
import { useAuthStore } from '@/lib/stores';

/**
 * 可复用的登录表单（客户端组件）
 * 用于首页和 /login 页面，包含完整的提交流程与 API 调用
 */
export function LoginForm() {
  const t = useTranslations('auth');
  const tCommon = useTranslations('common');
  const router = useRouter();
  const params = useParams();
  const locale = (params?.locale as string) || 'zh-CN';
  const { isAuthenticated } = useAuthStore();

  const [employeeNo, setEmployeeNo] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 已登录时在 effect 中重定向，避免在渲染时调用 router 导致 React 报错
  useEffect(() => {
    if (isAuthenticated) {
      router.replace(`/${locale}/dashboard`);
    }
  }, [isAuthenticated, router, locale]);

  if (isAuthenticated) {
    return null;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

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
      router.replace(`/${locale}/dashboard`);
    } catch (err) {
      setError(err instanceof Error ? err.message : t('loginFailed'));
    } finally {
      setLoading(false);
    }
  };

  return (
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
  );
}
