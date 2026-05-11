import { useState, useEffect } from 'react'
import { Search, ShieldCheck, Snowflake, Unlock } from 'lucide-react'
import { getUsers, updateUserStatus, approveUser } from '@/services/admin'
import { toast } from 'sonner'

export default function AdminUsers() {
  const [users, setUsers] = useState<any[]>([])
  const [keyword, setKeyword] = useState('')

  const loadData = async () => {
    try {
      const res = await getUsers({ page: 1, keyword })
      setUsers(res.list || [])
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  // biome-ignore lint/correctness/useExhaustiveDependencies: loadData is stable
  useEffect(() => {
    loadData()
  }, [])

  const handleStatus = async (id: string, status: number) => {
    try {
      await updateUserStatus(id, status)
      toast.success(status === 2 ? '已解冻' : '已冻结')
      loadData()
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  const handleApprove = async (id: string) => {
    try {
      await approveUser(id)
      toast.success('已审批通过')
      loadData()
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  const roleMap: Record<number, string> = { 1: '管理员', 2: '店铺用户', 3: '普通用户' }
  const statusMap: Record<number, string> = { 1: '待审核', 2: '正常', 3: '已冻结' }

  const getRoleBadge = (role: number) => {
    const config: Record<number, { bg: string; color: string }> = {
      1: { bg: 'rgba(168, 85, 247, 0.15)', color: '#a855f7' },
      2: { bg: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6' },
      3: { bg: 'rgba(100, 116, 139, 0.15)', color: 'var(--text-muted)' },
    }
    const c = config[role] || config[3]
    return (
      <span style={{ background: c.bg, color: c.color }} className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium">
        <span style={{ background: c.color }} className="w-1.5 h-1.5 rounded-full" />
        {roleMap[role]}
      </span>
    )
  }

  const getStatusBadge = (status: number) => {
    const config: Record<number, string> = {
      1: 'pending',
      2: 'active',
      3: 'inactive',
    }
    return (
      <span className={`status-badge ${config[status] || config[1]}`}>
        {statusMap[status]}
      </span>
    )
  }

  return (
    <div className="p-6 space-y-5">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-bold" style={{ color: 'var(--text-primary)' }}>用户管理</h1>
        <p className="text-sm mt-1" style={{ color: 'var(--text-secondary)' }}>管理所有用户</p>
      </div>

      {/* Main Card */}
      <div className="glass-card p-5">
        {/* Card Header with Search */}
        <div className="border-b" style={{ borderColor: 'var(--card-border)' }}>
          <div className="flex items-center gap-3">
            <div
              className="flex items-center gap-2 px-4 py-2.5 flex-1"
              style={{
                background: 'var(--card-bg)',
                border: '1px solid var(--card-border)',
                borderRadius: '12px',
              }}
            >
              <Search className="w-4 h-4" style={{ color: 'var(--text-muted)' }} />
              <input
                type="text"
                placeholder="搜索用户"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && loadData()}
                className="flex-1 bg-transparent text-sm outline-none"
                style={{ color: 'var(--text-primary)' }}
              />
            </div>
            <button
              type="button"
              onClick={loadData}
              className="px-4 py-2.5 rounded-xl border-0 font-medium"
              style={{
                background: 'var(--accent)',
                color: '#fff',
                boxShadow: '0 4px 16px var(--accent-glow)',
              }}
            >
              搜索
            </button>
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto">
          <table className="w-full glass-table">
            <thead>
              <tr>
                <th className="text-left text-xs font-medium p-4 text-sm" style={{ color: 'var(--text-muted)' }}>ID</th>
                <th className="text-left text-xs font-medium p-4 text-sm" style={{ color: 'var(--text-muted)' }}>用户名</th>
                <th className="text-left text-xs font-medium p-4 text-sm" style={{ color: 'var(--text-muted)' }}>昵称</th>
                <th className="text-left text-xs font-medium p-4 text-sm" style={{ color: 'var(--text-muted)' }}>角色</th>
                <th className="text-left text-xs font-medium p-4 text-sm" style={{ color: 'var(--text-muted)' }}>状态</th>
                <th className="text-left text-xs font-medium p-4 text-sm" style={{ color: 'var(--text-muted)' }}>操作</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td className="p-4 text-sm font-mono" style={{ color: 'var(--text-secondary)' }}>{user.id}</td>
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      <div
                        className="w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium"
                        style={{ background: 'var(--accent-light)', color: 'var(--accent)' }}
                      >
                        {user.username?.charAt(0) || 'U'}
                      </div>
                      <span className="text-sm font-medium" style={{ color: 'var(--text-primary)' }}>{user.username}</span>
                    </div>
                  </td>
                  <td className="p-4 text-sm" style={{ color: 'var(--text-secondary)' }}>{user.nickname}</td>
                  <td className="p-4">{getRoleBadge(user.role)}</td>
                  <td className="p-4">{getStatusBadge(user.status)}</td>
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      {user.status === 1 && (
                        <button
                          type="button"
                          onClick={() => handleApprove(user.id)}
                          className="px-4 py-2 rounded-xl border-0 font-medium flex items-center gap-1.5 hover:scale-105 transition-transform"
                          style={{
                            background: '#10b981',
                            color: '#fff',
                            boxShadow: '0 4px 16px rgba(16,185,129,0.3)',
                          }}
                        >
                          <ShieldCheck className="w-4 h-4" />
                          审批通过
                        </button>
                      )}
                      {user.status === 2 && (
                        <button
                          type="button"
                          onClick={() => handleStatus(user.id, 3)}
                          className="w-9 h-9 rounded-xl border flex items-center justify-center hover:scale-105 transition-transform"
                          style={{
                            background: 'var(--card-bg)',
                            borderColor: 'var(--card-border)',
                            color: '#f43f5e',
                          }}
                          title="冻结"
                        >
                          <Snowflake className="w-4 h-4" />
                        </button>
                      )}
                      {user.status === 3 && (
                        <button
                          type="button"
                          onClick={() => handleStatus(user.id, 2)}
                          className="w-9 h-9 rounded-xl border flex items-center justify-center hover:scale-105 transition-transform"
                          style={{
                            background: 'var(--card-bg)',
                            borderColor: 'var(--card-border)',
                            color: '#f59e0b',
                          }}
                          title="解冻"
                        >
                          <Unlock className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}