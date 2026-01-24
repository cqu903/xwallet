# xWallet Front-Web

xWallet Web 管理后台，基于 Next.js 14 + React 18 + TypeScript + shadcn/ui。

## 技术栈

- Next.js 14 (App Router)
- React 18
- TypeScript
- Tailwind CSS v4
- shadcn/ui
- Zustand (状态管理)
- SWR (数据获取)
- next-intl (国际化)

## 环境变量（可选）

前端项目默认使用硬编码的 API 地址 `http://localhost:8080/api`。如需自定义配置，可创建 `.env.local` 文件：

```env
# .env.local（可选）
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_APP_NAME=xWallet
```

**注意：**
- `.env.local` 文件已加入 `.gitignore`，不会提交到版本控制
- 大多数情况下不需要创建此文件，使用默认配置即可

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
