import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { getMyShop, applyShop, updateShopStatus } from '@/services/operator'
import { toast } from 'sonner'

export default function OperatorShop() {
  const [shop, setShop] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState({ name: '', description: '' })

  const loadData = async () => {
    try {
      const res = await getMyShop()
      setShop(res)
    } catch (err: any) {
      // 没有店铺
    }
  }

  const handleApply = async () => {
    if (!form.name) {
      toast.error('请输入店铺名称')
      return
    }
    setLoading(true)
    try {
      await applyShop({ name: form.name, description: form.description })
      toast.success('申请已提交')
      loadData()
    } catch (err: any) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleToggleStatus = async () => {
    try {
      await updateShopStatus(shop.isActive === 1 ? 0 : 1)
      toast.success(shop.isActive === 1 ? '已歇业' : '已营业')
      loadData()
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  const statusMap: Record<number, string> = { 1: '待审核', 2: '已通过', 3: '已拒绝', 4: '已禁用' }

  // Status badge color mapping
  const getStatusStyle = (status: number) => {
    switch (status) {
      case 1: // pending
        return { background: '#f59e0b', color: '#fff' }
      case 2: // approved
        return { background: '#10b981', color: '#fff' }
      case 3: // rejected
        return { background: 'var(--text-muted)', color: '#fff' }
      case 4: // disabled
        return { background: 'var(--text-muted)', color: '#fff' }
      default:
        return { background: 'var(--text-muted)', color: '#fff' }
    }
  }

  return (
    <div className="p-6 space-y-4">
      <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)' }}>我的店铺</h1>

      {!shop ? (
        <Card glass>
          <CardHeader>
            <CardTitle>申请店铺</CardTitle>
            <p style={{ fontSize: '14px', color: 'var(--text-primary)', marginTop: '4px' }}>申请成为店主</p>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label style={{ fontSize: '14px', color: 'var(--text-primary)' }}>店铺名称</Label>
              <Input className="glass-input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="请输入店铺名称" />
            </div>
            <div className="space-y-2">
              <Label style={{ fontSize: '14px', color: 'var(--text-primary)' }}>店铺简介</Label>
              <Input className="glass-input" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="请输入店铺简介" />
            </div>
            <Button className="glass-btn glass-btn-primary" onClick={handleApply} disabled={loading}>提交申请</Button>
          </CardContent>
        </Card>
      ) : (
        <Card glass>
          <CardHeader className="flex flex-row items-center justify-between">
            <div className="flex items-center gap-4">
              <CardTitle>{shop.name}</CardTitle>
              <Badge className="status-badge" style={getStatusStyle(shop.status)}>{statusMap[shop.status]}</Badge>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div style={{ borderRadius: '12px', background: 'var(--card-bg)', border: '1px solid var(--card-border)', padding: '16px' }}>
              <p style={{ fontSize: '14px', color: 'var(--text-muted)' }}>店铺简介</p>
              <p style={{ color: 'var(--text-primary)', marginTop: '4px' }}>{shop.description || '暂无简介'}</p>
            </div>
            <div className="flex gap-2">
              <Button
                className="glass-btn"
                onClick={handleToggleStatus}
              >
                {shop.isActive === 1 ? '歇业' : '营业'}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}