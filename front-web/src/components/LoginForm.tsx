'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useTranslations } from 'next-intl';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { login } from '@/lib/api/auth';
import { useAuthStore } from '@/lib/stores';
import { Eye, EyeOff } from 'lucide-react';

interface LoginError {
  message: string;
  remainingAttempts?: number;
  lockoutTime?: number;
}

/**
 * 可复用的登录表单（客户端组件）
 * 用于首页和 /login 页面，包含完整的提交流程与 API 调用
 * 支持"记住我"功能和登录失败重试逻辑
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
  const [rememberMe, setRememberMe] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<LoginError | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const [lockoutCountdown, setLockoutCountdown] = useState(0);
  const employeeNoRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);

  // 处理锁定倒计时
  useEffect(() => {
    if (lockoutCountdown > 0) {
      const timer = setInterval(() => {
        setLockoutCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
      return () => clearInterval(timer);
    }
  }, [lockoutCountdown]);

  // 已登录时在 effect 中重定向，避免在渲染时调用 router 导致 React 报错
  useEffect(() => {
    if (isAuthenticated) {
      router.replace(`/${locale}/dashboard`);
    }
  }, [isAuthenticated, router, locale]);

  // 错误时自动聚焦
  useEffect(() => {
    if (error) {
      if (employeeNo.trim() === '') {
        employeeNoRef.current?.focus();
      } else if (password.trim() === '') {
        passwordRef.current?.focus();
      }
    }
  }, [error, employeeNo, password]);

  if (isAuthenticated) {
    return null;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!employeeNo.trim()) {
      setError({ message: t('pleaseInputEmployeeNo') });
      employeeNoRef.current?.focus();
      return;
    }
    if (!password.trim()) {
      setError({ message: t('pleaseInputPassword') });
      passwordRef.current?.focus();
      return;
    }

    if (lockoutCountdown > 0) {
      setError({ message: t('accountLocked', { seconds: lockoutCountdown }) });
      return;
    }

    setLoading(true);
    try {
      await login({ employeeNo, password, rememberMe });
      router.replace(`/${locale}/dashboard`);
    } catch (err) {
      const loginError = err as { message?: string; remainingAttempts?: number; lockoutTime?: number };

      if (loginError.remainingAttempts !== undefined) {
        setError({
          message: t('loginFailedWithRetry', { count: loginError.remainingAttempts }),
          remainingAttempts: loginError.remainingAttempts,
        });
      } else if (loginError.lockoutTime !== undefined) {
        setLockoutCountdown(loginError.lockoutTime);
        setError({
          message: t('accountLocked', { seconds: loginError.lockoutTime }),
          lockoutTime: loginError.lockoutTime,
        });
      } else {
        setError({ message: loginError.message || t('loginFailed') });
      }
    } finally {
      setLoading(false);
    }
  };

  // 处理回车键提交
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !loading) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border p-6">
      {error && (
        <div className="rounded-md bg-destructive/15 p-3 text-sm text-destructive">
          {error.message}
        </div>
      )}

      <div className="space-y-2">
        <Label htmlFor="employeeNo">{t('employeeNo')}</Label>
        <Input
          ref={employeeNoRef}
          id="employeeNo"
          type="text"
          value={employeeNo}
          onChange={(e) => setEmployeeNo(e.target.value)}
          placeholder={t('employeeNoPlaceholder')}
          disabled={loading}
          autoComplete="username"
          onKeyDown={handleKeyDown}
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="password">{t('password')}</Label>
        <div className="relative">
          <Input
            ref={passwordRef}
            id="password"
            type={showPassword ? 'text' : 'password'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder={t('passwordPlaceholder')}
            disabled={loading}
            autoComplete="current-password"
            onKeyDown={handleKeyDown}
            className="pr-10"
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            disabled={loading}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground disabled:opacity-50"
            tabIndex={-1}
          >
            {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        </div>
      </div>

      <div className="flex items-center space-x-2">
        <Checkbox
          id="rememberMe"
          checked={rememberMe}
          onCheckedChange={(checked) => setRememberMe(checked as boolean)}
          disabled={loading}
        />
        <Label
          htmlFor="rememberMe"
          className="text-sm font-normal cursor-pointer"
        >
          {t('rememberMe')}
        </Label>
      </div>

      <Button
        type="submit"
        className="w-full"
        disabled={loading || lockoutCountdown > 0}
      >
        {loading ? (
          <>
            <span className="animate-spin mr-2">⏳</span>
            {tCommon('loading')}
          </>
        ) : lockoutCountdown > 0 ? (
          t('locked', { seconds: lockoutCountdown })
        ) : (
          t('login')
        )}
      </Button>
    </form>
  );
}
