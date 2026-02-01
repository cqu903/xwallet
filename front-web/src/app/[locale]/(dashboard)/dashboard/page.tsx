'use client';

import { useTranslations } from 'next-intl';
import { useAuthStore } from '@/lib/stores';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export default function DashboardPage() {
  const t = useTranslations();
  const { user } = useAuthStore();

  const stats = [
    {
      title: t('dashboard.totalUsers'),
      value: '1,234',
      change: '+20.1%',
      changeLabel: '较上月',
      trend: 'up',
      icon: (
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
      ),
      gradient: 'from-violet-500/20 to-violet-500/5',
      iconBg: 'bg-violet-500/10',
      iconText: 'text-violet-500',
    },
    {
      title: t('dashboard.totalRoles'),
      value: '12',
      change: '+2',
      changeLabel: '新增角色',
      trend: 'up',
      icon: (
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
        </svg>
      ),
      gradient: 'from-amber-500/20 to-amber-500/5',
      iconBg: 'bg-amber-500/10',
      iconText: 'text-amber-500',
    },
    {
      title: t('dashboard.todayLogs'),
      value: '573',
      change: '+201',
      changeLabel: '较上小时',
      trend: 'up',
      icon: (
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
        </svg>
      ),
      gradient: 'from-green-500/20 to-green-500/5',
      iconBg: 'bg-green-500/10',
      iconText: 'text-green-500',
    },
    {
      title: '系统状态',
      value: '正常',
      change: '99.9%',
      changeLabel: '可用性',
      trend: 'stable',
      icon: (
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
        </svg>
      ),
      gradient: 'from-blue-500/20 to-blue-500/5',
      iconBg: 'bg-blue-500/10',
      iconText: 'text-blue-500',
    },
  ];

  const activities = [
    { user: 'ADMIN001', action: '登录系统', time: '2 分钟前', type: 'login' },
    { user: 'SYSTEM', action: '创建新角色 "测试角色"', time: '15 分钟前', type: 'create' },
    { user: 'ADMIN001', action: '更新用户 USER001 信息', time: '1 小时前', type: 'update' },
    { user: 'USER002', action: '修改权限配置', time: '2 小时前', type: 'permission' },
    { user: 'SYSTEM', action: '执行数据备份', time: '3 小时前', type: 'backup' },
  ];

  const quickActions = [
    {
      title: '添加用户',
      description: '创建新的系统用户',
      icon: (
        <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
        </svg>
      ),
      href: '/users',
      color: 'primary',
    },
    {
      title: '权限管理',
      description: '配置角色和权限',
      icon: (
        <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
        </svg>
      ),
      href: '/system/roles',
      color: 'amber',
    },
    {
      title: '查看报表',
      description: '数据分析与统计',
      icon: (
        <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
      ),
      href: '/reports',
      color: 'green',
    },
  ];

  const getActivityIcon = (type: string) => {
    const icons = {
      login: {
        icon: (
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
          </svg>
        ),
        bgColor: 'bg-green-500/10',
        textColor: 'text-green-500',
      },
      create: {
        icon: (
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
        ),
        bgColor: 'bg-primary/10',
        textColor: 'text-primary',
      },
      update: {
        icon: (
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
        ),
        bgColor: 'bg-primary/10',
        textColor: 'text-primary',
      },
      permission: {
        icon: (
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
          </svg>
        ),
        bgColor: 'bg-amber-500/10',
        textColor: 'text-amber-500',
      },
      backup: {
        icon: (
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
          </svg>
        ),
        bgColor: 'bg-blue-500/10',
        textColor: 'text-blue-500',
      },
    };

    return icons[type as keyof typeof icons] || icons.login;
  };

  const getQuickActionColor = (color: string) => {
    const colors = {
      primary: {
        bg: 'bg-primary/10',
        text: 'text-primary',
        hoverBg: 'group-hover:bg-primary/20',
      },
      amber: {
        bg: 'bg-amber-500/10',
        text: 'text-amber-500',
        hoverBg: 'group-hover:bg-amber-500/20',
      },
      green: {
        bg: 'bg-green-500/10',
        text: 'text-green-500',
        hoverBg: 'group-hover:bg-green-500/20',
      },
    };

    return colors[color as keyof typeof colors] || colors.primary;
  };

  return (
    <div className="space-y-6 md:space-y-8 animate-fade-in">
      {/* 欢迎横幅 - 优化视觉层次和对比度 */}
      <div className="relative overflow-hidden rounded-2xl border border-primary/20 shadow-lg bg-gradient-to-br from-primary/10 to-primary/5 dark:from-primary dark:via-primary/95 dark:to-primary/90">
        <div className="absolute inset-0 bg-grid opacity-10 dark:opacity-10" />
        <div className="relative flex flex-col gap-4 p-6 sm:flex-row sm:items-center sm:justify-between sm:gap-6 md:p-8">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-primary/20 dark:bg-white/20 backdrop-blur-sm shadow-inner transition-transform hover:scale-105">
              <svg className="h-7 w-7 text-primary dark:text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
              </svg>
            </div>
            <div className="space-y-1">
              <h1 className="font-display text-xl font-bold text-foreground dark:text-white sm:text-2xl lg:text-3xl">
                欢迎回来，{user?.username || 'Admin'}
              </h1>
              <p className="text-muted-foreground dark:text-white/90 text-sm sm:text-base">
                今日系统运行平稳，所有服务正常在线
              </p>
            </div>
          </div>
          <div className="flex flex-wrap gap-2 sm:gap-3">
            <span className="inline-flex items-center gap-2 rounded-lg bg-primary/10 dark:bg-white/15 px-3 py-2 text-sm font-medium text-primary dark:text-white backdrop-blur-sm transition-colors hover:bg-primary/20 dark:hover:bg-white/20">
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              上次登录: {new Date().toLocaleDateString()}
            </span>
            <span className="inline-flex items-center gap-2 rounded-lg bg-primary/10 dark:bg-white/15 px-3 py-2 text-sm font-medium text-primary dark:text-white backdrop-blur-sm transition-colors hover:bg-primary/20 dark:hover:bg-white/20">
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01" />
              </svg>
              系统版本: hotfix-0.1.0
            </span>
          </div>
        </div>
      </div>

      {/* 统计卡片 - 优化布局和交互 */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat, index) => (
          <Card
            key={index}
            className={`group relative overflow-hidden rounded-xl border border-border/50 bg-card shadow-sm transition-all duration-200 hover:shadow-md hover:${stat.iconBg.replace('/10', '/30')}`}
          >
            <div className={`absolute inset-0 bg-gradient-to-br ${stat.gradient} opacity-0 transition-opacity duration-200 group-hover:opacity-100`} />
            <CardHeader className="relative pb-3">
              <div className="flex items-start justify-between gap-3">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  {stat.title}
                </CardTitle>
                <div className={`rounded-lg ${stat.iconBg} p-2 ${stat.iconText} transition-all duration-200 group-hover:bg-current group-hover:text-background`}>
                  {stat.icon}
                </div>
              </div>
            </CardHeader>
            <CardContent className="relative space-y-3 pt-0">
              <div className="text-3xl font-bold tracking-tight number-animate tabular-nums">
                {stat.value}
              </div>
              <div className="flex items-center gap-2">
                {stat.trend === 'up' && (
                  <svg className="h-4 w-4 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                  </svg>
                )}
                <span className="text-sm font-semibold text-green-500 tabular-nums">
                  {stat.change}
                </span>
                <span className="text-xs text-muted-foreground">
                  {stat.changeLabel}
                </span>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* 最近活动 - 优化可读性和可访问性 */}
        <Card className="lg:col-span-2 rounded-xl border border-border/50 bg-card shadow-sm">
          <CardHeader className="border-b border-border/50 pb-4">
            <div className="flex items-center justify-between gap-4">
              <div className="space-y-1">
                <CardTitle className="font-display text-xl">
                  {t('dashboard.recentActivities')}
                </CardTitle>
                <p className="text-sm text-muted-foreground">
                  实时追踪系统操作日志
                </p>
              </div>
              <button
                className="shrink-0 rounded-lg bg-primary/10 px-4 py-2 text-sm font-medium text-primary transition-all duration-200 hover:bg-primary/20 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2"
                aria-label="查看全部活动"
              >
                查看全部
              </button>
            </div>
          </CardHeader>
          <CardContent className="p-4 sm:p-6">
            <div className="space-y-2">
              {activities.map((activity, index) => {
                const { icon, bgColor, textColor } = getActivityIcon(activity.type);
                return (
                  <div
                    key={index}
                    className="group flex items-start gap-4 rounded-lg p-3 transition-all duration-200 hover:bg-muted/50 focus-within:bg-muted/50"
                  >
                    <div className={`shrink-0 rounded-lg ${bgColor} p-2 ${textColor} transition-all duration-200 group-hover:scale-110`}>
                      {icon}
                    </div>
                    <div className="min-w-0 flex-1 space-y-0.5">
                      <p className="truncate text-sm font-medium text-foreground">
                        <span className="font-semibold text-primary">{activity.user}</span>
                        {' '}{activity.action}
                      </p>
                      <p className="text-xs text-muted-foreground">{activity.time}</p>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      <div className="h-2 w-2 rounded-full bg-primary animate-pulse" aria-hidden="true" />
                    </div>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>

        {/* 快捷操作 - 优化交互反馈 */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="font-display text-lg font-semibold text-foreground">
              快捷操作
            </h2>
          </div>
          <div className="space-y-3">
            {quickActions.map((action, index) => {
              const { bg, text, hoverBg } = getQuickActionColor(action.color);
              return (
                <a
                  key={index}
                  href={action.href}
                  className="group block rounded-xl border border-border/50 bg-card p-4 shadow-sm transition-all duration-200 hover:shadow-md hover:border-primary/30 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2"
                >
                  <div className="flex items-center gap-4">
                    <div className={`shrink-0 rounded-xl ${bg} p-3 ${text} transition-all duration-200 ${hoverBg} group-hover:scale-110`}>
                      {action.icon}
                    </div>
                    <div className="min-w-0 flex-1">
                      <h3 className="truncate text-base font-semibold text-foreground transition-colors group-hover:text-primary">
                        {action.title}
                      </h3>
                      <p className="truncate text-sm text-muted-foreground">
                        {action.description}
                      </p>
                    </div>
                    <svg
                      className="h-5 w-5 shrink-0 text-muted-foreground transition-all duration-200 group-hover:text-primary group-hover:translate-x-1"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                      aria-hidden="true"
                    >
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                    </svg>
                  </div>
                </a>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
