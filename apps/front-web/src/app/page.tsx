import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

export default function Home() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <div className="w-full max-w-md space-y-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-foreground">
            xWallet 管理后台
          </h1>
          <p className="mt-2 text-sm text-muted-foreground">
            Front-Web 基础架构测试页面
          </p>
        </div>

        <div className="space-y-4 rounded-lg border p-6">
          <div className="space-y-2">
            <Label htmlFor="employeeNo">工号</Label>
            <Input id="employeeNo" placeholder="请输入工号" />
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">密码</Label>
            <Input id="password" type="password" placeholder="请输入密码" />
          </div>

          <div className="flex gap-2">
            <Button className="flex-1">登录</Button>
            <Button variant="outline" className="flex-1">
              取消
            </Button>
          </div>

          <div className="flex gap-2">
            <Button variant="secondary" size="sm">
              Small
            </Button>
            <Button variant="secondary" size="default">
              Default
            </Button>
            <Button variant="secondary" size="lg">
              Large
            </Button>
          </div>

          <div className="flex gap-2">
            <Button variant="default">Default</Button>
            <Button variant="secondary">Secondary</Button>
            <Button variant="outline">Outline</Button>
            <Button variant="ghost">Ghost</Button>
            <Button variant="destructive">Destructive</Button>
          </div>
        </div>

        <div className="text-center text-sm text-muted-foreground">
          <p>✅ Next.js 14 + TypeScript</p>
          <p>✅ Tailwind CSS + shadcn/ui</p>
          <p>✅ 基础架构搭建完成</p>
        </div>
      </div>
    </div>
  )
}
