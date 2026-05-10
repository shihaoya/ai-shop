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

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">我的店铺</h1>

      {!shop ? (
        <Card>
          <CardHeader>
            <CardTitle>申请店铺</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label>店铺名称</Label>
              <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="请输入店铺名称" />
            </div>
            <div className="space-y-2">
              <Label>店铺简介</Label>
              <Input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="请输入店铺简介" />
            </div>
            <Button onClick={handleApply} disabled={loading}>提交申请</Button>
          </CardContent>
        </Card>
      ) : (
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <div className="flex items-center gap-4">
              <CardTitle>{shop.name}</CardTitle>
              <Badge>{statusMap[shop.status]}</Badge>
            </div>
            {shop.status === 2 && (
              <Button variant="outline" onClick={handleToggleStatus}>
                {shop.isActive === 1 ? '切换歇业' : '切换营业'}
              </Button>
            )}
          </CardHeader>
          <CardContent>
            <p className="text-neutral-500">{shop.description}</p>
          </CardContent>
        </Card>
      )}
    </div>
  )
}