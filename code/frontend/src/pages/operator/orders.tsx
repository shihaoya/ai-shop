import { useState, useEffect } from 'react'
import { Package } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from 'sonner'
import { getOrders, type Order } from '@/services/operator'

const STATUS_MAP: Record<number, { label: string; variant: string }> = {
  1: { label: '待处理', variant: 'secondary' },
  2: { label: '已完成', variant: 'default' },
  3: { label: '已关闭', variant: 'outline' },
}

export default function OperatorOrders() {
  const [loading, setLoading] = useState(true)
  const [orders, setOrders] = useState<Order[]>([])

  useEffect(() => { loadData() }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await getOrders()
      setOrders(res.list || [])
    } catch (err: any) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">订单管理</h1>
      <Card glass>
        <CardContent className="p-0">
          {loading ? (
            <div className="p-4 space-y-2">
              {[1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="glass-skeleton h-12" />)}
            </div>
          ) : orders.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground">
              <Package className="w-12 h-12 mx-auto mb-4 opacity-50" />
              <p>暂无订单</p>
            </div>
          ) : (
            <Table className="glass-table">
              <TableHeader>
                <TableRow>
                  <TableHead>订单ID</TableHead>
                  <TableHead>商品</TableHead>
                  <TableHead>用户</TableHead>
                  <TableHead>积分</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead>时间</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {orders.map((o) => (
                  <TableRow key={o.id}>
                    <TableCell className="font-mono text-xs">{o.id}</TableCell>
                    <TableCell>{o.productId}</TableCell>
                    <TableCell>{o.userId}</TableCell>
                    <TableCell>{o.points}</TableCell>
                    <TableCell>
                      <Badge className="glass-badge" variant={STATUS_MAP[o.status]?.variant as any || 'secondary'}>
                        {STATUS_MAP[o.status]?.label || '未知'}
                      </Badge>
                    </TableCell>
                    <TableCell>{o.createTime ? new Date(o.createTime).toLocaleString() : '-'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  )
}