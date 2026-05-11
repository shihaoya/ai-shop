import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Store, Search, Check, X } from 'lucide-react'
import { getShops, auditShop } from '@/services/admin'
import { toast } from 'sonner'

export default function AdminShops() {
  const [shops, setShops] = useState<any[]>([])
  const [params, setParams] = useSearchParams({ page: '1', keyword: '', status: '' })
  const [keyword, setKeyword] = useState(params.get('keyword') || '')
  const [statusFilter, setStatusFilter] = useState(params.get('status') || '')

  const loadData = async () => {
    try {
      const status = statusFilter ? Number(statusFilter) : undefined
      const res = await getShops({ page: Number(params.get('page')), keyword: params.get('keyword') || '', status })
      setShops(res.list || [])
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  useEffect(() => {
    loadData()
  }, [params, statusFilter])

  const handleSearch = () => {
    setParams({ page: '1', keyword, status: statusFilter })
  }

  const handleStatusFilterChange = (value: string) => {
    setStatusFilter(value)
    setParams({ page: '1', keyword, status: value })
  }

  const getStatusBadge = (status: number) => {
    const config: Record<number, { className: string; label: string }> = {
      1: { className: 'pending', label: '待审核' },
      2: { className: 'active', label: '已通过' },
      3: { className: 'inactive', label: '已拒绝' },
      4: { className: 'inactive', label: '已禁用' },
    }
    const c = config[status] || config[1]
    return <span className={`status-badge ${c.className}`}>{c.label}</span>
  }

  return (
    <div className="p-6 space-y-5">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-bold" style={{ color: 'var(--text-primary)' }}>店铺管理</h1>
        <p className="text-sm mt-1" style={{ color: 'var(--text-secondary)' }}>管理所有店铺申请</p>
      </div>

      {/* Main Card */}
      <div className="glass-card p-5">
        {/* Card Header with Search */}
        <div className="border-b" style={{ borderColor: 'var(--card-border)' }}>
          <div className="flex items-center gap-3 flex-wrap py-4">
            <div className="glass-input flex items-center gap-2 flex-1 min-w-[200px]">
              <Search className="w-4 h-4" style={{ color: 'var(--text-muted)' }} />
              <input
                type="text"
                placeholder="搜索店铺名称"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                className="flex-1 bg-transparent text-sm outline-none"
                style={{ color: 'var(--text-primary)' }}
              />
            </div>

            {/* 状态筛选 */}
            <select
              value={statusFilter}
              onChange={(e) => handleStatusFilterChange(e.target.value)}
              className="px-4 py-2.5 rounded-xl border text-sm"
              style={{
                background: 'var(--card-bg)',
                borderColor: 'var(--card-border)',
                color: 'var(--text-primary)',
              }}
            >
              <option value="">全部状态</option>
              <option value="1">待审核</option>
              <option value="2">已通过</option>
              <option value="3">已拒绝</option>
              <option value="4">已禁用</option>
            </select>

            <button
              type="button"
              onClick={handleSearch}
              className="btn-primary"
            >
              搜索
            </button>
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto">
          <table className="glass-table w-full">
            <thead>
              <tr>
                <th className="text-left text-xs font-medium p-4" style={{ color: 'var(--text-muted)' }}>ID</th>
                <th className="text-left text-xs font-medium p-4" style={{ color: 'var(--text-muted)' }}>店铺名称</th>
                <th className="text-left text-xs font-medium p-4" style={{ color: 'var(--text-muted)' }}>运营者</th>
                <th className="text-left text-xs font-medium p-4" style={{ color: 'var(--text-muted)' }}>状态</th>
                <th className="text-left text-xs font-medium p-4" style={{ color: 'var(--text-muted)' }}>申请时间</th>
                <th className="text-left text-xs font-medium p-4" style={{ color: 'var(--text-muted)' }}>操作</th>
              </tr>
            </thead>
            <tbody>
              {shops.map((shop) => (
                <tr key={shop.id} className="border-t" style={{ borderColor: 'var(--card-border)' }}>
                  <td className="p-4 text-sm font-mono" style={{ color: 'var(--text-secondary)' }}>{shop.id}</td>
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      <div
                        className="w-8 h-8 rounded-lg flex items-center justify-center"
                        style={{ background: 'var(--accent-light)' }}
                      >
                        <Store className="w-4 h-4" style={{ color: 'var(--accent)' }} />
                      </div>
                      <span className="text-sm font-medium" style={{ color: 'var(--text-primary)' }}>{shop.name}</span>
                    </div>
                  </td>
                  <td className="p-4 text-sm" style={{ color: 'var(--text-secondary)' }}>{shop.operatorUsername}</td>
                  <td className="p-4">{getStatusBadge(shop.status)}</td>
                  <td className="p-4 text-sm" style={{ color: 'var(--text-muted)' }}>{shop.createTime}</td>
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      {shop.status === 1 && (
                        <>
                          <button
                            type="button"
                            onClick={() => handleAudit(shop.id, 2)}
                            className="action-btn"
                            style={{ color: '#10b981' }}
                          >
                            <Check className="w-4 h-4" />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleAudit(shop.id, 3)}
                            className="action-btn"
                            style={{ color: '#f43f5e' }}
                          >
                            <X className="w-4 h-4" />
                          </button>
                        </>
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