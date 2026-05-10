import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { UserPlus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { toast } from 'sonner'
import { register } from '@/services/auth'

export default function RegisterPage() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState({
    username: '',
    nickname: '',
    password: '',
    confirmPassword: '',
    inviteCode: '',
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    // 密码确认
    if (form.password !== form.confirmPassword) {
      toast.error('两次输入的密码不一致')
      return
    }

    // 密码长度校验
    if (form.password.length < 6) {
      toast.error('密码长度不能少于6位')
      return
    }

    setLoading(true)
    try {
      await register({
        username: form.username,
        nickname: form.nickname,
        password: form.password,
        inviteCode: form.inviteCode,
      })
      toast.success('注册成功，请登录')
      navigate('/auth/login')
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : '注册失败'
      toast.error(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-neutral-950 to-neutral-900 p-4">
      <Card className="w-full max-w-md bg-neutral-900 border-neutral-800">
        <CardHeader className="space-y-1">
          <CardTitle className="text-2xl font-bold text-center">注册账号</CardTitle>
          <CardDescription className="text-center">创建新账号开始积分兑换</CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username">用户名</Label>
              <Input
                id="username"
                placeholder="请输入用户名"
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="nickname">昵称</Label>
              <Input
                id="nickname"
                placeholder="请输入昵称"
                value={form.nickname}
                onChange={(e) => setForm({ ...form, nickname: e.target.value })}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">密码</Label>
              <Input
                id="password"
                type="password"
                placeholder="请输入密码"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">确认密码</Label>
              <Input
                id="confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                value={form.confirmPassword}
                onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="inviteCode">邀请码</Label>
              <Input
                id="inviteCode"
                placeholder="请输入邀请码（可选）"
                value={form.inviteCode}
                onChange={(e) => setForm({ ...form, inviteCode: e.target.value })}
              />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col gap-4">
            <Button type="submit" className="w-full" disabled={loading}>
              <UserPlus className="w-4 h-4 mr-2" />
              {loading ? '注册中...' : '注册'}
            </Button>
            <p className="text-sm text-neutral-400 text-center">
              已有账号？<Link to="/auth/login" className="text-accent hover:underline">立即登录</Link>
            </p>
          </CardFooter>
        </form>
      </Card>
    </div>
  )
}