import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { LogIn, Eye, EyeOff, User, Lock } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { toast } from 'sonner'
import { login } from '@/services/auth'
import { useAuthStore } from '@/stores/auth'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login: setAuth } = useAuthStore()
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [form, setForm] = useState({ username: '', password: '' })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await login(form) as any
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
    <div className="w-full max-w-[420px] relative z-10">
      {/* Card */}
      <div
        className="relative rounded-[20px] overflow-hidden"
        style={{
          background: 'var(--card-bg)',
          border: '1px solid var(--card-border)',
          backdropFilter: 'blur(24px)',
          WebkitBackdropFilter: 'blur(24px)',
          boxShadow: '0 0 0 1px rgba(59, 130, 246, 0.1), 0 20px 50px rgba(0, 0, 0, 0.5), 0 0 80px rgba(59, 130, 246, 0.08)',
          transition: 'background 0.3s, border-color 0.3s, box-shadow 0.3s',
        }}
      >
        {/* Top accent bar */}
        <div
          className="h-[3px]"
          style={{
            background: 'linear-gradient(90deg, transparent, var(--accent), transparent)',
            boxShadow: '0 0 20px var(--accent-glow)',
          }}
        />

        {/* Inner border */}
        <div
          className="absolute inset-[1px] rounded-[20px] pointer-events-none"
          style={{ border: '1px solid rgba(255, 255, 255, 0.05)' }}
        />

        {/* Card body */}
        <div style={{ padding: '36px 40px 32px' }}>
          <h1 className="text-[26px] font-bold mb-1.5" style={{ color: 'var(--text-primary)', letterSpacing: '-0.5px', transition: 'color 0.3s' }}>
            积分商城
          </h1>
          <p className="text-sm mb-8" style={{ color: 'var(--text-secondary)', transition: 'color 0.3s' }}>
            登录以继续您的兑换之旅
          </p>

          <form onSubmit={handleSubmit}>
            {/* Username field */}
            <div className="relative mb-5">
              <input
                id="login-username"
                type="text"
                placeholder=" "
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                required
                className="peer w-full h-[56px] rounded-xl text-[15px] outline-none"
                style={{
                  background: 'var(--input-bg)',
                  border: '1px solid var(--input-border)',
                  color: 'var(--text-primary)',
                  padding: '16px 16px 0',
                  transition: 'border-color 0.2s, box-shadow 0.2s, background 0.3s, color 0.3s',
                }}
              />
              <label
                htmlFor="login-username"
                className="absolute left-4 top-1/2 -translate-y-1/2 text-[15px] pointer-events-none flex items-center gap-2 transition-all duration-200"
                style={{ transform: 'none', top: '10px', fontSize: '11px', color: 'var(--accent)', fontWeight: 500 }}
              >
                <User className="w-[13px] h-[13px] opacity-70" />
                用户名
              </label>
            </div>

            {/* Password field */}
            <div className="relative mb-5">
              <div className="relative">
                <input
                  id="login-password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder=" "
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  required
                  className="peer w-full h-[56px] rounded-xl text-[15px] outline-none pr-10"
                  style={{
                    background: 'var(--input-bg)',
                    border: '1px solid var(--input-border)',
                    color: 'var(--text-primary)',
                    padding: '16px 44px 0 16px',
                    transition: 'border-color 0.2s, box-shadow 0.2s, background 0.3s, color 0.3s',
                  }}
                />
                <label
                  htmlFor="login-password"
                  className="absolute left-4 top-1/2 -translate-y-1/2 text-[15px] pointer-events-none flex items-center gap-2 transition-all duration-200"
style={{ transform: 'none', top: '10px', fontSize: '11px', color: 'var(--accent)', fontWeight: 500 }}
                >
                  <Lock className="w-[13px] h-[13px] opacity-70" />
                  密码
                </label>
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 p-1 transition-colors"
                  style={{ color: 'var(--text-secondary)' }}
                  onMouseEnter={(e) => { e.currentTarget.style.color = 'var(--text-primary)' }}
                  onMouseLeave={(e) => { e.currentTarget.style.color = 'var(--text-secondary)' }}
                >
                  {showPassword ? (
                    <EyeOff className="w-[14px] h-[14px]" />
                  ) : (
                    <Eye className="w-[14px] h-[14px]" />
                  )}
                </button>
              </div>
            </div>

            {/* Submit button */}
            <Button
              type="submit"
              className="w-full h-[52px] rounded-xl text-[15px] font-semibold text-white transition-all duration-200 active:scale-[0.97] flex items-center justify-center gap-2"
              disabled={loading}
              style={{
                background: 'var(--accent)',
                boxShadow: '0 4px 16px var(--accent-glow)',
                transition: 'transform 0.2s, box-shadow 0.2s, filter 0.2s, background 0.3s',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'translateY(-2px)'
                e.currentTarget.style.boxShadow = '0 8px 24px var(--accent-glow)'
                e.currentTarget.style.filter = 'brightness(1.05)'
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = ''
                e.currentTarget.style.boxShadow = '0 4px 16px var(--accent-glow)'
                e.currentTarget.style.filter = ''
              }}
            >
              <LogIn className="w-[15px] h-[15px]" />
              {loading ? '登录中...' : '登 录'}
            </Button>
          </form>
        </div>

        {/* Card footer */}
        <div style={{ padding: '0 40px 32px', textAlign: 'center' }}>
          <p className="text-xs" style={{ color: 'var(--text-secondary)', marginTop: '16px', transition: 'color 0.3s' }}>
            还没有账号？<Link to="/auth/register" style={{ color: 'var(--accent)', textDecoration: 'none', fontWeight: 500 }}>立即注册</Link>
          </p>
        </div>
      </div>
    </div>
  )
}