import { redirect } from 'next/navigation';

export default async function Home({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  // 重定向到专业的登录页面
  redirect(`/${locale}/login`);
}
