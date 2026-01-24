import { getTranslations } from 'next-intl/server';
import { LoginForm } from '@/components/LoginForm';

export async function generateMetadata({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const t = await getTranslations({ locale: (await params).locale });

  return {
    title: t('auth.loginTitle'),
  };
}

export default async function Home({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const t = await getTranslations({ locale: (await params).locale });

  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <div className="w-full max-w-md space-y-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-foreground">
            {t('auth.loginTitle')}
          </h1>
          <p className="mt-2 text-sm text-muted-foreground">
            {t('auth.loginSubtitle')}
          </p>
        </div>

        <LoginForm />

        <div className="text-center text-sm text-muted-foreground">
          <p>✅ Next.js 14 + TypeScript</p>
          <p>✅ Tailwind CSS + shadcn/ui</p>
          <p>✅ next-intl 国际化</p>
          <p>✅ 主题系统 (next-themes)</p>
        </div>
      </div>
    </div>
  );
}
