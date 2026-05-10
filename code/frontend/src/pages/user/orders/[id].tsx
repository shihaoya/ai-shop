import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, Package, MapPin, XCircle, CheckCircle, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from 'sonner'
import { getOrder, closeOrder, completeOrder, type Order } from '@/services/user'

const STATUS_MAP: Record<number, { label: string; variant: 'default' | 'secondary' | 'outline' | 'destructive' }> = {
  1: { label: '待处理', variant: 'secondary' },
  2: { label: '已完成', variant: 'default' },
  3: { label: '已关闭', variant: 'outline' },
}

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [loading, setLoading] = useState(true)
  const [order, setOrder] = useState<Order | null>(null)
  const [actionLoading, setActionLoading] = useState(false)

  const fetchOrder = async () => {
    if (!id) return
    setLoading(true)
    try {
      const res = await getOrder(id)
      setOrder(res)
    } catch (err: any) {
      toast.error(err.message || '获取订单详情失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchOrder()
  }, [id])

  const handleClose = async () => {
    if (!order) return
    setActionLoading(true)
    try {
      await closeOrder(order.id)
      toast.success('订单已关闭')
      fetchOrder()
    } catch (err: any) {
      toast.error(err.message || '关闭订单失败')
    } finally {
      setActionLoading(false)
    }
  }

  const handleComplete = async () => {
    if (!order) return
    setActionLoading(true)
    try {
      await completeOrder(order.id)
      toast.success('订单已完成')
      fetchOrder()
    } catch (err: any) {
      toast.error(err.message || '完成订单失败')
    } finally {
      setActionLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="max-w-2xl mx-auto p-4">
        <Skeleton className="h-6 w-32 mb-6" />
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-1/3" />
          </CardHeader>
          <CardContent className="space-y-4">
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
          </CardContent>
        </Card>
      </div>
    )
  }

  if (!order) {
    return (
      <div className="max-w-2xl mx-auto p-4 text-center py-12">
        <p className="text-muted-foreground">订单不存在</p>
        <Link to="/user/orders">
          <Button className="mt-4">返回订单列表</Button>
        </Link>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto p-4">
      {/* Back Button */}
      <Link to="/user/orders">
        <Button variant="ghost" className="mb-6 gap-2">
          <ArrowLeft className="w-4 h-4" />
          返回订单列表
        </Button>
      </Link>

      <Card>
        <CardHeader className="pb-4">
          <div className="flex items-center justify-between">
            <CardTitle>订单详情</CardTitle>
            <Badge variant={STATUS_MAP[order.status]?.variant || 'secondary'}>
              {STATUS_MAP[order.status]?.label || '未知'}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Product Info */}
          <div className="flex gap-4 p-4 bg-muted/30 rounded-lg">
            <div className="w-20 h-20 rounded bg-muted flex-shrink-0 overflow-hidden">
              {order.product?.image ? (
                <img src={order.product.image} alt={order.product?.name} className="w-full h-full object-cover" />
              ) : (
                <Package className="w-10 h-10 m-auto text-muted-foreground/50" />
              )}
            </div>
            <div className="flex-1">
              <p className="font-medium">{order.product?.name || '商品'}</p>
              <p className="text-sm text-muted-foreground mt-1">
                {order.product?.description}
              </p>
              <p className="text-accent font-semibold mt-2">{order.points} 积分</p>
            </div>
          </div>

          {/* Address Info */}
          {order.address && (
            <div className="flex gap-3 p-4 bg-muted/30 rounded-lg">
              <MapPin className="w-5 h-5 text-muted-foreground flex-shrink-0 mt-0.5" />
              <div className="text-sm">
                <p className="font-medium">{order.address.name} {order.address.phone}</p>
                <p className="text-muted-foreground mt-1">
                  {order.address.province}{order.address.city}{order.address.district}{order.address.detail}
                </p>
              </div>
            </div>
          )}

          {/* Order Time */}
          <div className="text-sm text-muted-foreground">
            <p>下单时间：{order.createTime ? new Date(order.createTime).toLocaleString() : '-'}</p>
            {order.updateTime && order.status !== 1 && (
              <p className="mt-1">更新时间：{new Date(order.updateTime).toLocaleString()}</p>
            )}
          </div>

          {/* Actions */}
          {order.status === 1 && (
            <div className="flex gap-3 pt-4 border-t">
              <Button
                variant="outline"
                className="gap-2"
                disabled={actionLoading}
                onClick={handleClose}
              >
                {actionLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : <XCircle className="w-4 h-4" />}
                关闭订单
              </Button>
              <Button
                className="gap-2"
                disabled={actionLoading}
                onClick={handleComplete}
              >
                {actionLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : <CheckCircle className="w-4 h-4" />}
                确认收货
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}