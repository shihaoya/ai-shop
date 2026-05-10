import { useState, useEffect } from 'react'
import { KeyRound, Copy, RefreshCw } from 'lucide-react'
import { getInviteCode, generateInviteCode } from '@/services/admin'
import { toast } from 'sonner'

export default function AdminInviteCode() {
  const [code, setCode] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const loadCode = async () => {
      setLoading(true)
      try {
        const res = await getInviteCode()
        setCode(res.code || '')
      } catch {
        // 忽略错误，可能还没有邀请码
      } finally {
        setLoading(false)
      }
    }
    loadCode()
  }, [])

  const handleCreate = async () => {
    setLoading(true)
    try {
      const res = await generateInviteCode()
      setCode(res.code || '')
      toast.success('邀请码已生成')
    } catch (err: any) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleCopy = () => {
    navigator.clipboard.writeText(code)
    toast.success('已复制')
  }

  return (
    <div style={{ padding: '24px' }}>
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 700, color: 'var(--text-primary)' }}>邀请码管理</h1>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '4px' }}>管理员邀请码</p>
      </div>

      <div style={{
        background: 'var(--card-bg)',
        border: '1px solid var(--card-border)',
        borderRadius: '20px',
        backdropFilter: 'blur(20px)',
        overflow: 'hidden'
      }}>
        {/* Card header */}
        <div style={{
          padding: '18px 24px',
          background: 'rgba(15,23,42,0.5)',
          borderBottom: '1px solid var(--card-border)',
          display: 'flex',
          alignItems: 'center',
          gap: '8px'
        }}>
          <KeyRound size={18} style={{ color: 'var(--accent)' }} />
          <span style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>我的邀请码</span>
        </div>

        {/* Card body */}
        <div style={{ padding: '24px' }}>
          {/* Code display area */}
          <div style={{
            background: 'rgba(30,41,59,0.6)',
            border: '1px solid var(--card-border)',
            borderRadius: '12px',
            padding: '14px 18px',
            fontFamily: 'monospace',
            fontSize: '18px',
            fontWeight: 600,
            color: 'var(--accent)',
            textAlign: 'center',
            letterSpacing: '0.1em',
            marginBottom: '16px'
          }}>
            {code || '点击生成获取邀请码'}
          </div>

          {/* Buttons row */}
          <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
            <button
              type="button"
              onClick={handleCopy}
              disabled={!code}
              style={{
                width: '40px',
                height: '40px',
                borderRadius: '12px',
                border: '1px solid var(--card-border)',
                background: 'var(--card-bg)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                cursor: code ? 'pointer' : 'not-allowed',
                color: code ? 'var(--accent)' : 'var(--text-muted)',
                transition: 'all 0.2s'
              }}
              onMouseEnter={(e) => { if (code) e.currentTarget.style.background = 'var(--accent-light)' } }
              onMouseLeave={(e) => { e.currentTarget.style.background = 'var(--card-bg)' } }
            >
              <Copy size={18} />
            </button>

            <button
              type="button"
              onClick={handleCreate}
              disabled={loading}
              style={{
                background: 'var(--accent)',
                color: '#fff',
                borderRadius: '12px',
                padding: '10px 20px',
                fontSize: '14px',
                fontWeight: 500,
                border: 'none',
                cursor: loading ? 'not-allowed' : 'pointer',
                opacity: loading ? 0.7 : 1,
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                boxShadow: '0 4px 14px rgba(59,130,246,0.35)'
              }}
            >
              <RefreshCw size={16} style={{ opacity: loading ? 0.7 : 1 }} />
              {loading ? '生成中...' : '生成新邀请码'}
            </button>
          </div>

          {/* Note */}
          <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginTop: '12px' }}>
            重新生成会使旧邀请码失效
          </p>
        </div>
      </div>
    </div>
  )
}