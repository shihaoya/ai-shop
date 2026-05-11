import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { UserPlus, Eye, EyeOff, User, Lock, Mail, Key } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { toast } from 'sonner'
import { register } from '@/services/auth'

export default function RegisterPage() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [form, setForm] = useState({
    username: '',
    nickname: '',
    password: '',
    confirmPassword: '',
    inviteCode: '',
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (form.password !== form.confirmPassword) {
      toast.error('两次输入的密码不一致')
      return
    }

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
        confirmPassword: form.confirmPassword,
        inviteCode: form.inviteCode,
      })
      toast.success('注册成功，请登录')
      navigate('/auth/login')
    } catch {
      // 错误已由 request 拦截器的 toast 提示，无需重复处理
    } finally {
      setLoading(false)
    }
  }

  const inputStyle = {
    background: 'var(--input-bg)',
    border: '1px solid var(--input-border)',
    color: 'var(--text-primary)',
    transition: 'border-color 0.2s, box-shadow 0.2s, background 0.3s, color 0.3s',
  }

  const floatingLabelStyle = (float: boolean) => ({
    top: float ? '10px' : '50%',
    transform: float ? 'none' : 'translateY(-50%)',
    fontSize: float ? '11px' : '14px',
    color: float ? 'var(--accent)' : 'var(--text-secondary)',
    fontWeight: float ? 500 : 400,
    transition: 'all 0.18s ease',
  })

  return (
    <div className="w-full max-w-[440px] relative z-10">
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
        <div style={{ padding: '32px 36px 28px' }}>
          <h1 className="text-[24px] font-bold mb-1" style={{ color: 'var(--text-primary)', letterSpacing: '-0.5px', transition: 'color 0.3s' }}>
            创建账号
          </h1>
          <p className="text-[13px] mb-7" style={{ color: 'var(--text-secondary)', transition: 'color 0.3s' }}>
            填写信息完成注册
          </p>

          <form onSubmit={handleSubmit}>
            {/* Form grid */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              {/* Username */}
              <div className="relative" style={{ marginBottom: '16px' }}>
                <input
                  id="reg-username"
                  type="text"
                  placeholder=" "
                  value={form.username}
                  onChange={(e) => setForm({ ...form, username: e.target.value })}
                  required
                  className="peer w-full h-[52px] rounded-xl text-[14px] outline-none"
                  style={{ ...inputStyle, padding: '14px 14px 0' }}
                />
                <label
                  htmlFor="reg-username"
                  className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[14px] pointer-events-none flex items-center gap-2"
                  style={floatingLabelStyle(!!form.username)}
                >
                  <User className="w-[12px] h-[12px] opacity-70" />
                  用户名
                </label>
              </div>

              {/* Nickname */}
              <div className="relative" style={{ marginBottom: '16px' }}>
                <input
                  id="reg-nickname"
                  type="text"
                  placeholder=" "
                  value={form.nickname}
                  onChange={(e) => setForm({ ...form, nickname: e.target.value })}
                  required
                  className="peer w-full h-[52px] rounded-xl text-[14px] outline-none"
                  style={{ ...inputStyle, padding: '14px 14px 0' }}
                />
                <label
                  htmlFor="reg-nickname"
                  className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[14px] pointer-events-none flex items-center gap-2"
                  style={floatingLabelStyle(!!form.nickname)}
                >
                  <Mail className="w-[12px] h-[12px] opacity-70" />
                  昵称
                </label>
              </div>
            </div>

            {/* Password */}
            <div className="relative mb-4">
              <div className="relative">
                <input
                  id="reg-password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder=" "
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  required
                  className="peer w-full h-[52px] rounded-xl text-[14px] outline-none pr-10"
                  style={{ ...inputStyle, padding: '14px 44px 0 14px' }}
                />
                <label
                  htmlFor="reg-password"
                  className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[14px] pointer-events-none flex items-center gap-2"
                  style={floatingLabelStyle(!!form.password)}
                >
                  <Lock className="w-[12px] h-[12px] opacity-70" />
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

            {/* Confirm Password */}
            <div className="relative mb-4">
              <div className="relative">
                <input
                  id="reg-confirm-password"
                  type={showConfirmPassword ? 'text' : 'password'}
                  placeholder=" "
                  value={form.confirmPassword}
                  onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
                  required
                  className="peer w-full h-[52px] rounded-xl text-[14px] outline-none pr-10"
                  style={{ ...inputStyle, padding: '14px 44px 0 14px' }}
                />
                <label
                  htmlFor="reg-confirm-password"
                  className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[14px] pointer-events-none flex items-center gap-2"
                  style={floatingLabelStyle(!!form.confirmPassword)}
                >
                  <Lock className="w-[12px] h-[12px] opacity-70" />
                  确认密码
                </label>
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 p-1 transition-colors"
                  style={{ color: 'var(--text-secondary)' }}
                  onMouseEnter={(e) => { e.currentTarget.style.color = 'var(--text-primary)' }}
                  onMouseLeave={(e) => { e.currentTarget.style.color = 'var(--text-secondary)' }}
                >
                  {showConfirmPassword ? (
                    <EyeOff className="w-[14px] h-[14px]" />
                  ) : (
                    <Eye className="w-[14px] h-[14px]" />
                  )}
                </button>
              </div>
            </div>

            {/* Invite Code */}
            <div className="relative mb-6">
              <input
                id="reg-invite-code"
                type="text"
                placeholder=" "
                value={form.inviteCode}
                onChange={(e) => setForm({ ...form, inviteCode: e.target.value })}
                className="peer w-full h-[52px] rounded-xl text-[14px] outline-none"
                style={{ ...inputStyle, padding: '14px 14px 0' }}
              />
              <label
                htmlFor="reg-invite-code"
                className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[14px] pointer-events-none flex items-center gap-2"
                style={floatingLabelStyle(!!form.inviteCode)}
              >
                <Key className="w-[12px] h-[12px] opacity-70" />
                邀请码（可选）
              </label>
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
              <UserPlus className="w-[15px] h-[15px]" />
              {loading ? '注册中...' : '注 册'}
            </Button>
          </form>
        </div>

        {/* Card footer */}
        <div style={{ padding: '0 36px 28px', textAlign: 'center' }}>
          <p className="text-[13px]" style={{ color: 'var(--text-secondary)', marginTop: '16px', transition: 'color 0.3s' }}>
            已有账号？<Link to="/auth/login" style={{ color: 'var(--accent)', textDecoration: 'none', fontWeight: 500 }}>立即登录</Link>
          </p>
        </div>
      </div>
    </div>
  )
}