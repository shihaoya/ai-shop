import { useState, useEffect } from 'react'
import { KeyRound, Copy, RefreshCw, Check } from 'lucide-react'
import { getInviteCode, generateInviteCode } from '@/services/admin'
import { toast } from 'sonner'

export default function AdminInviteCode() {
  const [code, setCode] = useState('')
  const [loading, setLoading] = useState(false)
  const [loadingInit, setLoadingInit] = useState(true)
  const [copied, setCopied] = useState(false)

  useEffect(() => {
    loadCode()
  }, [])

  const loadCode = async () => {
    setLoadingInit(true)
    try {
      const res: any = await getInviteCode()
      if (typeof res === 'string') {
        setCode(res)
      } else if (res?.code) {
        setCode(res.code)
      }
    } catch {
      // 还没有邀请码
    } finally {
      setLoadingInit(false)
    }
  }

  const handleCreate = async () => {
    setLoading(true)
    try {
      const res: any = await generateInviteCode()
      if (typeof res === 'string') {
        setCode(res)
        toast.success('邀请码已生成')
      } else if (res?.code) {
        setCode(res.code)
        toast.success('邀请码已生成')
      }
    } catch (err: any) {
      toast.error(err.message || '生成失败')
    } finally {
      setLoading(false)
    }
  }

  const handleCopy = () => {
    if (!code) return
    navigator.clipboard.writeText(code)
    setCopied(true)
    toast.success('已复制到剪贴板')
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div style={{ padding: '24px' }}>
      {/* Page Header */}
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)' }}>邀请码管理</h1>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '4px' }}>管理员邀请码</p>
      </div>

      {/* Loading skeleton */}
      {loadingInit ? (
        <div
          className="glass-card"
          style={{ padding: '24px', textAlign: 'center' }}
        >
          <div style={{ color: 'var(--text-muted)', fontSize: '14px' }}>加载中...</div>
        </div>
      ) : (
        <div
          style={{
            maxWidth: '520px',
          }}
        >
          <div
            style={{
              background: 'var(--card-bg)',
              border: '1px solid var(--card-border)',
              borderRadius: '20px',
              backdropFilter: 'blur(20px)',
              overflow: 'hidden',
            }}
          >
            {/* Gradient accent bar */}
            <div
              style={{
                height: '4px',
                background: 'linear-gradient(90deg, var(--accent), #a78bfa, var(--accent))',
                backgroundSize: '200% 100%',
              }}
            />

            {/* Card header */}
            <div
              style={{
                padding: '20px 24px',
                borderBottom: '1px solid var(--card-border)',
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
              }}
            >
              <div
                style={{
                  width: '36px',
                  height: '36px',
                  borderRadius: '10px',
                  background: 'var(--accent-light)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <KeyRound size={18} style={{ color: 'var(--accent)' }} />
              </div>
              <div>
                <div style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>
                  管理员邀请码
                </div>
                <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '2px' }}>
                  用于邀请新管理员注册
                </div>
              </div>
            </div>

            {/* Card body */}
            <div style={{ padding: '28px 24px' }}>
              {/* Code display */}
              <div
                style={{
                  background: 'linear-gradient(135deg, rgba(59,130,246,0.08), rgba(139,92,246,0.08))',
                  border: '1px solid var(--card-border)',
                  borderRadius: '14px',
                  padding: '24px',
                  textAlign: 'center',
                  marginBottom: '20px',
                  position: 'relative',
                }}
              >
                {code ? (
                  <>
                    <div
                      style={{
                        fontFamily: 'monospace',
                        fontSize: '28px',
                        fontWeight: 700,
                        color: 'var(--accent)',
                        letterSpacing: '0.15em',
                        marginBottom: '8px',
                        userSelect: 'all',
                      }}
                    >
                      {code}
                    </div>
                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                      复制此邀请码并发送给新管理员
                    </div>
                  </>
                ) : (
                  <div
                    style={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      gap: '12px',
                      padding: '8px 0',
                    }}
                  >
                    <KeyRound size={36} style={{ color: 'var(--text-muted)', opacity: 0.4 }} />
                    <div>
                      <div style={{ fontSize: '15px', fontWeight: 500, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                        暂无邀请码
                      </div>
                      <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
                        点击下方按钮生成一个新的邀请码
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* Buttons */}
              <div style={{ display: 'flex', gap: '12px' }}>
                <button
                  type="button"
                  onClick={handleCopy}
                  disabled={!code}
                  style={{
                    flex: 1,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '8px',
                    padding: '12px 20px',
                    borderRadius: '12px',
                    fontSize: '14px',
                    fontWeight: 500,
                    border: `1px solid var(--card-border)`,
                    background: code ? 'var(--card-bg)' : 'var(--card-bg)',
                    color: code ? 'var(--text-primary)' : 'var(--text-muted)',
                    cursor: code ? 'pointer' : 'not-allowed',
                    opacity: code ? 1 : 0.5,
                    transition: 'all 0.2s',
                  }}
                  onMouseEnter={(e) => {
                    if (code) {
                      e.currentTarget.style.borderColor = 'var(--accent)'
                      e.currentTarget.style.background = 'var(--accent-light)'
                    }
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = 'var(--card-border)'
                    e.currentTarget.style.background = 'var(--card-bg)'
                  }}
                >
                  {copied ? <Check size={16} style={{ color: '#10b981' }} /> : <Copy size={16} />}
                  {copied ? '已复制' : '复制邀请码'}
                </button>

                <button
                  type="button"
                  onClick={handleCreate}
                  disabled={loading}
                  style={{
                    flex: 1,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '8px',
                    padding: '12px 20px',
                    borderRadius: '12px',
                    fontSize: '14px',
                    fontWeight: 500,
                    border: 'none',
                    background: 'var(--accent)',
                    color: '#fff',
                    cursor: loading ? 'not-allowed' : 'pointer',
                    opacity: loading ? 0.7 : 1,
                    boxShadow: '0 4px 14px var(--accent-glow)',
                    transition: 'all 0.2s',
                  }}
                  onMouseEnter={(e) => {
                    if (!loading) {
                      e.currentTarget.style.opacity = '0.9'
                      e.currentTarget.style.transform = 'translateY(-1px)'
                    }
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.opacity = '1'
                    e.currentTarget.style.transform = 'translateY(0)'
                  }}
                >
                  <RefreshCw
                    size={16}
                    style={{
                      animation: loading ? 'spin 1s linear infinite' : 'none',
                    }}
                  />
                  {loading ? '生成中...' : '生成新邀请码'}
                </button>
              </div>

              {/* Note */}
              <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginTop: '16px', textAlign: 'center' }}>
                重新生成将使之前的邀请码失效
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
