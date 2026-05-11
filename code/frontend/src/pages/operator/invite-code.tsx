import { useState, useEffect } from 'react'
import { KeyRound, Copy, RefreshCw } from 'lucide-react'
import { getInviteCode, generateInviteCode } from '@/services/operator'
import { toast } from 'sonner'

export default function OperatorInviteCode() {
  const [code, setCode] = useState('')
  const [loading, setLoading] = useState(false)
  const [loadingInit, setLoadingInit] = useState(true)

  useEffect(() => {
    loadCode()
  }, [])

  const loadCode = async () => {
    setLoadingInit(true)
    try {
      const res = await getInviteCode()
      if (res && res.code) {
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
      const res = await generateInviteCode()
      if (res && res.code) {
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
    toast.success('已复制到剪贴板')
  }

  return (
    <div className="p-6 space-y-4">
      <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)' }}>
        邀请码管理
      </h1>

      <div
        style={{
          background: 'var(--card-bg)',
          border: '1px solid var(--card-border)',
          borderRadius: '20px',
          backdropFilter: 'blur(20px)',
          overflow: 'hidden',
        }}
      >
        {/* Card header */}
        <div
          style={{
            padding: '18px 24px',
            borderBottom: '1px solid var(--card-border)',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
          }}
        >
          <div
            style={{
              width: '32px',
              height: '32px',
              borderRadius: '10px',
              background: 'var(--accent-light)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <KeyRound size={16} style={{ color: 'var(--accent)' }} />
          </div>
          <span style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>
            我的邀请码
          </span>
        </div>

        {/* Card body */}
        <div style={{ padding: '32px 24px' }}>
          {loadingInit ? (
            <div style={{ textAlign: 'center', padding: '20px 0' }}>
              <div
                style={{
                  width: '200px',
                  height: '20px',
                  background: 'var(--card-border)',
                  borderRadius: '8px',
                  margin: '0 auto',
                  opacity: 0.5,
                }}
              />
            </div>
          ) : (
            <>
              {/* Code display */}
              <div
                style={{
                  background: 'rgba(30, 41, 59, 0.4)',
                  border: '1px solid var(--card-border)',
                  borderRadius: '14px',
                  padding: '16px 20px',
                  textAlign: 'center',
                  marginBottom: '20px',
                }}
              >
                {code ? (
                  <span
                    style={{
                      fontFamily: 'var(--font-mono)',
                      fontSize: '22px',
                      fontWeight: 700,
                      color: 'var(--accent)',
                      letterSpacing: '0.12em',
                    }}
                  >
                    {code}
                  </span>
                ) : (
                  <span style={{ fontSize: '15px', color: 'var(--text-muted)' }}>
                    暂无邀请码，点击下方按钮生成
                  </span>
                )}
              </div>

              {/* Buttons */}
              <div style={{ display: 'flex', gap: '12px', justifyContent: 'center' }}>
                <button
                  type="button"
                  onClick={handleCreate}
                  disabled={loading}
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '8px',
                    padding: '10px 22px',
                    borderRadius: '12px',
                    fontSize: '14px',
                    fontWeight: 500,
                    border: 'none',
                    cursor: loading ? 'not-allowed' : 'pointer',
                    background: 'var(--accent)',
                    color: '#fff',
                    opacity: loading ? 0.7 : 1,
                    boxShadow: '0 4px 14px var(--accent-glow)',
                    transition: 'all 0.2s',
                  }}
                  onMouseOver={(e) => { if (!loading) e.currentTarget.style.filter = 'brightness(1.1)' }}
                  onMouseOut={(e) => { e.currentTarget.style.filter = 'none' }}
                >
                  <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
                  {loading ? '生成中...' : '生成邀请码'}
                </button>

                {code && (
                  <button
                    type="button"
                    onClick={handleCopy}
                    style={{
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '8px',
                      padding: '10px 22px',
                      borderRadius: '12px',
                      fontSize: '14px',
                      fontWeight: 500,
                      border: '1px solid var(--card-border)',
                      cursor: 'pointer',
                      background: 'var(--card-bg)',
                      color: 'var(--text-primary)',
                      transition: 'all 0.2s',
                    }}
                    onMouseOver={(e) => {
                      e.currentTarget.style.background = 'var(--accent-light)'
                      e.currentTarget.style.borderColor = 'var(--accent)'
                      e.currentTarget.style.color = 'var(--accent)'
                    }}
                    onMouseOut={(e) => {
                      e.currentTarget.style.background = 'var(--card-bg)'
                      e.currentTarget.style.borderColor = 'var(--card-border)'
                      e.currentTarget.style.color = 'var(--text-primary)'
                    }}
                  >
                    <Copy size={16} />
                    复制邀请码
                  </button>
                )}
              </div>

              {code && (
                <p style={{ fontSize: '13px', color: 'var(--text-muted)', textAlign: 'center', marginTop: '16px' }}>
                  重新生成将使旧邀请码失效
                </p>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  )
}
