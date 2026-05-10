import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Package, Filter } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from 'sonner'
import { getOrders, type Order } from '@/services/user'

const STATUS_MAP: Record<number, { label: string; variant: 'default' | 'secondary' | 'outline' | 'destructive' }> = {
  1: { label: '待处理', variant: 'secondary' },
  2: { label: '已完成', variant: 'default' },
  3: { label: '已关闭', variant: 'outline' },
}

export default function OrdersPage() {
  const [loading, setLoading] = useState(true)
  const [orders, setOrders] = useState<Order[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize] = useState(10)
  const [statusFilter, setStatusFilter] = useState<number | undefined>(undefined)

  const fetchOrders = async () => {
    setLoading(true)
    try {
      const res = await getOrders({
        page,
        pageSize,
        ...(statusFilter !== undefined && { status: statusFilter }),
      })
      setOrders(res.list)
      setTotal(res.total)
    } catch (err: any) {
      toast.error(err.message || '获取订单列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchOrders()
  }, [page, statusFilter])

  const totalPages = Math.ceil(total / pageSize)

  return (
    <div className="max-w-4xl mx-auto p-4">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-semibold">我的订单</h1>
        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-muted-foreground" />
          <select
            value={statusFilter ?? ''}
            onChange={(e) => {
              setStatusFilter(e.target.value === '' ? undefined : Number(e.target.value))
              setPage(1)
            }}
            className="glass-input rounded-lg px-3 py-1.5 text-sm"
          >
            <option value="">全部</option>
            {Object.entries(STATUS_MAP).map(([value, { label }]) => (
              <option key={value} value={value}>{label}</option>
            ))}
          </select>
        </div>
      </div>

      {loading ? (
        <div className="space-y-4">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-20 w-full glass-skeleton" />
          ))}
        </div>
      ) : orders.length === 0 ? (
        <div className="text-center py-12 text-muted-foreground glass-card">
          <Package className="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>暂无订单</p>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <Link key={order.id} to={`/user/orders/${order.id}`}>
              <div className="glass-card rounded-lg p-4 hover:ring-2 hover:ring-accent/50 transition-all cursor-pointer">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex items-start gap-4">
                    <div className="w-16 h-16 rounded bg-muted flex-shrink-0 overflow-hidden">
                      {order.product?.image ? (
                        <img src={order.product.image} alt={order.product?.name} className="w-full h-full object-cover" />
                      ) : (
                        <Package className="w-8 h-8 m-auto text-muted-foreground/50" />
                      )}
                    </div>
                    <div className="space-y-1">
                      <p className="font-medium">{order.product?.name || '商品'}</p>
                      <p className="text-sm text-muted-foreground">
                        {order.product?.description}
                      </p>
                      <p className="text-sm text-accent font-medium">{order.points} 积分</p>
                    </div>
                  </div>
                  <div className="flex flex-col items-end gap-2">
                    <Badge variant={STATUS_MAP[order.status]?.variant || 'secondary'} className="glass-badge">
                      {STATUS_MAP[order.status]?.label || '未知'}
                    </Badge>
                    <span className="text-xs text-muted-foreground">
                      {order.createTime ? new Date(order.createTime).toLocaleDateString() : ''}
                    </span>
                  </div>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-6">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 1}
            onClick={() => setPage(page - 1)}
            className="glass-btn"
          >
            上一页
          </Button>
          <span className="px-3 py-2 text-sm glass">
            {page} / {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={page === totalPages}
            onClick={() => setPage(page + 1)}
            className="glass-btn"
          >
            下一页
          </Button>
        </div>
      )}
    </div>
  )
}