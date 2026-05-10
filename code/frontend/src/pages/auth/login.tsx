import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { LogIn } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { toast } from 'sonner'
import { login } from '@/services/auth'
import { useAuthStore } from '@/stores/auth'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login: setAuth } = useAuthStore()
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState({ username: '', password: '' })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await login(form) as any
      // 映射后端返回的 userinfo 字段到前端期望的格式
      const userInfo = {
        id: res.userinfo.id,
        username: res.userinfo.username,
        nickname: res.userinfo.nickname,
        role: res.userinfo.role,
        status: res.userinfo.status,
        points: res.userinfo.pointsBalance ? parseInt(res.userinfo.pointsBalance) : 0,
      }
      localStorage.setItem('token', res.token)
      setAuth(res.token, userInfo)
      toast.success('登录成功')

      // 根据角色跳转
      if (userInfo.role === 1) {
        navigate('/admin/dashboard')
      } else if (userInfo.role === 2) {
        navigate('/operator/dashboard')
      } else {
        navigate('/user/products')
      }
    } catch (err: any) {
      toast.error(err.message || '登录失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-neutral-800 via-neutral-900 to-neutral-950 p-4">
      <Card className="w-full max-w-md bg-neutral-50 border-neutral-200 shadow-xl">
        <CardHeader className="space-y-1">
          <CardTitle className="text-2xl font-bold text-center text-neutral-900">积分商城</CardTitle>
          <CardDescription className="text-center text-neutral-600">输入用户名和密码登录</CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username" className="text-neutral-700">用户名</Label>
              <Input
                id="username"
                placeholder="请输入用户名"
                style={{ backgroundColor: 'white', borderColor: '#d1d5db', color: '#111827' }}
                className="placeholder:text-neutral-400"
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password" className="text-neutral-700">密码</Label>
              <Input
                id="password"
                type="password"
                placeholder="请输入密码"
                style={{ backgroundColor: 'white', borderColor: '#d1d5db', color: '#111827' }}
                className="placeholder:text-neutral-400"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                required
              />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col gap-4">
            <Button type="submit" className="w-full bg-[#aa3bff] text-white hover:bg-[#9728eb]" disabled={loading}>
              <LogIn className="w-4 h-4 mr-2" />
              {loading ? '登录中...' : '登录'}
            </Button>
            <p className="text-sm text-neutral-500 text-center">
              还没有账号？<Link to="/auth/register" className="text-[#aa3bff] hover:underline">立即注册</Link>
            </p>
          </CardFooter>
        </form>
      </Card>
    </div>
  )
}