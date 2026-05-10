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

  const statusMap: Record<number, string> = { 1: '待审核', 2: '已通过', 3: '已拒绝', 4: '已禁用' }

  const getStatusBadge = (status: number) => {
    const config: Record<number, { className: string; dotClass: string }> = {
      1: { className: 'bg-amber-500/15 text-amber-500', dotClass: 'bg-amber-500' },
      2: { className: 'bg-emerald-500/15 text-emerald-500', dotClass: 'bg-emerald-500' },
      3: { className: 'bg-slate-500/15 text-slate-400', dotClass: 'bg-slate-500' },
      4: { className: 'bg-slate-500/15 text-slate-400', dotClass: 'bg-slate-500' },
    }
    const c = config[status] || config[1]
    return (
      <span className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium ${c.className}`}>
        <span className={`w-1.5 h-1.5 rounded-full ${c.dotClass}`} />
        {statusMap[status]}
      </span>
    )
  }

  return (
    <div className="p-6 space-y-5">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-bold" style={{ color: 'var(--text-primary)' }}>店铺管理</h1>
        <p className="text-sm mt-1" style={{ color: 'var(--text-secondary)' }}>管理所有店铺申请</p>
      </div>

      {/* Main Card */}
      <div
        className="rounded-2xl border backdrop-blur-xl"
        style={{
          background: 'var(--card-bg)',
          borderColor: 'var(--card-border)',
        }}
      >
        {/* Card Header with Search */}
        <div className="p-5 border-b" style={{ borderColor: 'var(--card-border)' }}>
          <div className="flex items-center gap-3">
            <div
              className="flex items-center gap-2 px-4 py-2.5 rounded-xl border flex-1"
              style={{
                background: 'var(--card-bg)',
                borderColor: 'var(--card-border)',
              }}
            >
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
          <table className="w-full">
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
                            className="w-9 h-9 rounded-xl border flex items-center justify-center hover:scale-105 transition-transform"
                            style={{
                              background: 'var(--card-bg)',
                              borderColor: 'var(--card-border)',
                              color: '#10b981',
                            }}
                          >
                            <Check className="w-4 h-4" />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleAudit(shop.id, 3)}
                            className="w-9 h-9 rounded-xl border flex items-center justify-center hover:scale-105 transition-transform"
                            style={{
                              background: 'var(--card-bg)',
                              borderColor: 'var(--card-border)',
                              color: '#f43f5e',
                            }}
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