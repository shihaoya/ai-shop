import { useState, useEffect } from 'react'
import { Copy, RefreshCw } from 'lucide-react'
import { getInviteCode, generateInviteCode } from '@/services/operator'
import { toast } from 'sonner'

export default function OperatorInviteCode() {
  const [code, setCode] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => { loadCode() }, [])

  const loadCode = async () => {
    setLoading(true)
    try {
      const res = await getInviteCode()
      setCode(res.code || '')
    } catch (err: any) {
      // 忽略错误
    } finally {
      setLoading(false)
    }
  }

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
    <div className="p-6 space-y-4">
      <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)' }}>
        邀请码管理
      </h1>

      <div
        className="glass-card"
        style={{ padding: '40px 32px', textAlign: 'center' }}
      >
        {/* Accent top bar */}
        <div
          style={{
            width: '48px',
            height: '4px',
            background: 'var(--accent)',
            borderRadius: '2px',
            margin: '0 auto 24px',
          }}
        />

        <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginBottom: '20px' }}>
          你的专属邀请码
        </p>

        {code ? (
          <div
            style={{
              fontSize: '28px',
              fontWeight: 700,
              fontFamily: 'var(--font-mono)',
              color: 'var(--accent)',
              letterSpacing: '0.15em',
              background: 'var(--accent-light)',
              border: '1px solid var(--card-border)',
              borderRadius: '16px',
              padding: '20px 24px',
              marginBottom: '28px',
              wordBreak: 'break-all',
            }}
          >
            {code}
          </div>
        ) : (
          <p style={{ fontSize: '16px', color: 'var(--text-muted)', marginBottom: '28px' }}>
            暂无邀请码
          </p>
        )}

        <div style={{ display: 'flex', justifyContent: 'center', gap: '12px' }}>
          <button
            type="button"
            className="btn-primary"
            onClick={handleCreate}
            disabled={loading}
          >
            <RefreshCw className="w-4 h-4" />
            {loading ? '生成中...' : '生成邀请码'}
          </button>
          {code && (
            <button type="button" className="btn-secondary" onClick={handleCopy}>
              <Copy className="w-4 h-4" />
              复制邀请码
            </button>
          )}
        </div>

        {code && (
          <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginTop: '20px' }}>
            重新生成将使旧邀请码失效
          </p>
        )}
      </div>
    </div>
  )
}
