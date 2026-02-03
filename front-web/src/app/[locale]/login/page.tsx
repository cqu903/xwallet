'use client';

import { useTranslations } from 'next-intl';
import { LoginForm } from '@/components/LoginForm';
import { ThemeToggle } from '@/components/ui/theme-toggle';

export default function LoginPage() {
  const t = useTranslations('auth');

  return (
    <div className="relative min-h-screen overflow-hidden bg-background bg-grid bg-pattern">
      {/* 主题切换按钮 */}
      <div className="absolute top-4 right-4 z-10">
        <ThemeToggle />
      </div>

      {/* 低调装饰光晕 */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 h-80 w-80 animate-pulse-glow rounded-full bg-primary/8 blur-3xl" />
        <div className="absolute -bottom-32 -left-32 h-72 w-72 rounded-full bg-primary/5 blur-3xl" />
      </div>

      <div className="relative flex min-h-screen items-center justify-center p-4">
        <div className="w-full max-w-5xl grid-cols-2 gap-16 lg:grid">
          {/* 左侧：品牌与简介 - 专业后台风格 */}
          <div className="hidden lg:flex flex-col justify-center space-y-8 animate-fade-in-up">
            <div className="space-y-6">
              <div className="flex items-center gap-3">
                <div className="flex h-11 w-11 items-center justify-center rounded-lg bg-primary shadow-md">
                  <svg className="h-6 w-6 text-primary-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
                <span className="font-display text-2xl font-semibold tracking-tight text-foreground">xWallet</span>
              </div>
              <h2 className="font-display text-3xl font-bold leading-tight text-foreground">
                钱包管理
                <span className="block text-primary">后台系统</span>
              </h2>
              <p className="max-w-sm text-base text-muted-foreground leading-relaxed">
                安全、高效的多端钱包管理解决方案，为运营与风控提供统一后台。
              </p>
            </div>
            <div className="flex gap-8 border-t border-border pt-6">
              <div>
                <div className="text-xl font-semibold text-primary">99.9%</div>
                <div className="text-xs text-muted-foreground">可用性</div>
              </div>
              <div>
                <div className="text-xl font-semibold text-primary">24/7</div>
                <div className="text-xs text-muted-foreground">技术支持</div>
              </div>
            </div>
          </div>

          {/* 右侧：登录表单 */}
          <div className="flex items-center justify-center animate-scale-in delay-200">
            <div className="w-full max-w-[400px]">
              <div className="rounded-xl border border-border bg-card p-8 shadow-sm space-y-6">
                <div className="flex lg:hidden items-center justify-center gap-2 pb-2">
                  <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary">
                    <svg className="h-5 w-5 text-primary-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                    </svg>
                  </div>
                  <span className="font-display text-xl font-semibold text-foreground">xWallet</span>
                </div>

                <div className="space-y-1">
                  <h1 className="font-display text-2xl font-bold text-foreground">
                    {t('loginTitle')}
                  </h1>
                  <p className="text-sm text-muted-foreground">
                    {t('loginSubtitle')}
                  </p>
                </div>

                <LoginForm />

                <div className="border-t border-border pt-4">
                  <div className="rounded-lg border border-border bg-muted/50 px-4 py-3 space-y-2">
                    <p className="text-xs font-medium text-muted-foreground">测试账号</p>
                    <p className="text-sm text-foreground">
                      工号 <span className="font-mono font-medium">ADMIN001</span>
                      <span className="mx-2 text-muted-foreground">/</span>
                      密码 <span className="font-mono font-medium">admin123</span>
                    </p>
                  </div>
                </div>
              </div>
              <p className="mt-4 text-center text-xs text-muted-foreground">
                登录即表示您同意服务条款与隐私政策
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}