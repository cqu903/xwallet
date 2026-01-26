'use client';

import { useTranslations } from 'next-intl';
import { LoginForm } from '@/components/LoginForm';

export default function LoginPage() {
  const t = useTranslations('auth');

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

        <LoginForm />

        <div className="text-center text-sm text-muted-foreground">
          <p>测试账号: ADMIN001 / admin123</p>
          <p>当前版本: release-0.1.0</p>
        </div>
      </div>
    </div>
  );
}
