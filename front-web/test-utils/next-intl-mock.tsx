import React from 'react';

export function useTranslations() {
  return (key: string) => key;
}

export function NextIntlClientProvider({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}
