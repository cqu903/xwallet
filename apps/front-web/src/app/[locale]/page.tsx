import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useTranslations } from 'next-intl';
import { getTranslations } from 'next-intl/server';

export async function generateMetadata({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const t = await getTranslations({ locale: (await params).locale, namespace: 'auth' });

  return {
    title: t('loginTitle'),
  };
}

export default async function Home({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const t = await getTranslations({ locale: (await params).locale, namespace: 'auth' });

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

        <div className="space-y-4 rounded-lg border p-6">
          <div className="space-y-2">
            <Label htmlFor="employeeNo">{t('employeeNo')}</Label>
            <Input id="employeeNo" placeholder={t('employeeNoPlaceholder')} />
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">{t('password')}</Label>
            <Input id="password" type="password" placeholder={t('passwordPlaceholder')} />
          </div>

          <div className="flex gap-2">
            <Button className="flex-1">{t('login')}</Button>
            <Button variant="outline" className="flex-1">
              {t('cancel')}
            </Button>
          </div>
        </div>

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
