import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Store, Search, Check, X } from 'lucide-react'
import { getShops, auditShop } from '@/services/admin'
import { toast } from 'sonner'

export default function AdminShops() {
  const [shops, setShops] = useState<any[]>([])
  const [params] = useSearchParams({ page: '1', keyword: '' })
  const [keyword, setKeyword] = useState(params.get('keyword') || '')

  const loadData = async () => {
    try {
      const res = await getShops({ page: Number(params.get('page')), keyword: params.get('keyword') || '' })
      setShops(res.list || [])
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  // biome-ignore lint/correctness/useExhaustiveDependencies: loadData is stable
  useEffect(() => {
    loadData()
  }, [params])

  const handleAudit = async (id: string, status: number) => {
    try {
      await auditShop(id, status)
      toast.success(status === 2 ? '已通过' : '已拒绝')
      loadData()
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  const handleSearch = () => {
    const url = new URL(window.location.href)
    url.searchParams.set('keyword', keyword)
    url.searchParams.set('page', '1')
    window.location.href = url.toString()
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
          <div className="flex items-center gap-3">
            <div className="glass-input flex items-center gap-2 flex-1">
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