import { useState, useEffect } from 'react'
import { Package, Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from 'sonner'
import { getProducts, createProduct, type Product } from '@/services/operator'

export default function OperatorProducts() {
  const [loading, setLoading] = useState(true)
  const [products, setProducts] = useState<Product[]>([])
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', description: '', points: '', stock: '', categoryId: '' })
  const [creating, setCreating] = useState(false)

  useEffect(() => { loadData() }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await getProducts()
      setProducts(res.list || [])
    } catch (err: any) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = async () => {
    if (!form.name || !form.points || !form.stock) {
      toast.error('请填写必填项')
      return
    }
    setCreating(true)
    try {
      const fd = new FormData()
      fd.append('name', form.name)
      fd.append('description', form.description)
      fd.append('points', form.points)
      fd.append('stock', form.stock)
      if (form.categoryId) fd.append('categoryId', form.categoryId)
      await createProduct(fd)
      toast.success('商品已创建')
      setShowForm(false)
      setForm({ name: '', description: '', points: '', stock: '', categoryId: '' })
      loadData()
    } catch (err: any) {
      toast.error(err.message)
    } finally {
      setCreating(false)
    }
  }

  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">商品管理</h1>
        <Button onClick={() => setShowForm(true)}><Plus className="h-4 w-4 mr-2" /> 添加商品</Button>
      </div>

      {showForm && (
        <Card>
          <CardContent className="space-y-4 p-6">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>商品名称</Label>
                <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
              </div>
              <div className="space-y-2">
                <Label>所需积分</Label>
                <Input type="number" value={form.points} onChange={(e) => setForm({ ...form, points: e.target.value })} />
              </div>
              <div className="space-y-2">
                <Label>库存</Label>
                <Input type="number" value={form.stock} onChange={(e) => setForm({ ...form, stock: e.target.value })} />
              </div>
              <div className="space-y-2">
                <Label>分类ID</Label>
                <Input value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })} placeholder="可选" />
              </div>
            </div>
            <div className="space-y-2">
              <Label>商品描述</Label>
              <Input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <div className="flex gap-2">
              <Button onClick={handleCreate} disabled={creating}>{creating ? '创建中...' : '创建'}</Button>
              <Button variant="outline" onClick={() => setShowForm(false)}>取消</Button>
            </div>
          </CardContent>
        </Card>
      )}

      {loading ? (
        <div className="grid grid-cols-3 gap-4">
          {[1, 2, 3, 4, 5, 6].map((i) => <Skeleton key={i} className="h-40" />)}
        </div>
      ) : products.length === 0 ? (
        <div className="text-center py-12 text-muted-foreground">
          <Package className="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>暂无商品</p>
        </div>
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {products.map((p) => (
            <Card key={p.id}>
              <CardContent className="p-4 space-y-2">
                <div className="w-full h-32 bg-muted rounded-lg flex items-center justify-center">
                  {p.image ? <img src={p.image} alt={p.name} className="w-full h-full object-cover" /> : <Package className="w-8 h-8 text-muted" />}
                </div>
                <p className="font-medium">{p.name}</p>
                <p className="text-sm text-muted-foreground">{p.description}</p>
                <div className="flex items-center justify-between">
                  <Badge variant="outline">{p.points} 积分</Badge>
                  <span className="text-sm text-muted-foreground">库存: {p.stock}</span>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}