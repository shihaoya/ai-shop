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
        <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)' }}>商品管理</h1>
        <Button className="glass-btn" onClick={() => setShowForm(true)}><Plus className="h-4 w-4 mr-2" /> 添加商品</Button>
      </div>

      {showForm && (
        <Card glass>
          <CardContent className="space-y-4 p-6">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label style={{ color: 'var(--text-primary)', fontSize: '14px', fontWeight: 500 }}>商品名称</Label>
                <Input className="glass-input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
              </div>
              <div className="space-y-2">
                <Label style={{ color: 'var(--text-primary)', fontSize: '14px', fontWeight: 500 }}>所需积分</Label>
                <Input className="glass-input" type="number" value={form.points} onChange={(e) => setForm({ ...form, points: e.target.value })} />
              </div>
              <div className="space-y-2">
                <Label style={{ color: 'var(--text-primary)', fontSize: '14px', fontWeight: 500 }}>库存</Label>
                <Input className="glass-input" type="number" value={form.stock} onChange={(e) => setForm({ ...form, stock: e.target.value })} />
              </div>
              <div className="space-y-2">
                <Label style={{ color: 'var(--text-primary)', fontSize: '14px', fontWeight: 500 }}>分类ID</Label>
                <Input className="glass-input" value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })} />
              </div>
            </div>
            <div className="space-y-2">
              <Label style={{ color: 'var(--text-primary)', fontSize: '14px', fontWeight: 500 }}>商品描述</Label>
              <Input className="glass-input" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <div className="flex gap-2">
              <Button className="glass-btn-primary" onClick={handleCreate} disabled={creating}>
                {creating ? '创建中...' : '创建'}
              </Button>
              <Button className="glass-btn" variant="outline" onClick={() => setShowForm(false)}>取消</Button>
            </div>
          </CardContent>
        </Card>
      )}

      <Card glass>
        <CardContent className="p-0">
          {loading ? (
            <div className="p-4 space-y-2">
              {[1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="glass-skeleton h-12" />)}
            </div>
          ) : products.length === 0 ? (
            <div className="text-center py-12" style={{ color: 'var(--text-muted)' }}>
              <Package className="w-12 h-12 mx-auto mb-4 opacity-50" />
              <p>暂无商品</p>
            </div>
          ) : (
            <div style={{ borderTop: '1px solid var(--card-border)' }}>
              {products.map((p) => (
                <div key={p.id} className="flex items-center justify-between p-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-3">
                      <span style={{ fontWeight: 500, color: 'var(--text-primary)' }}>{p.name}</span>
                      <Badge className="glass-badge" variant="outline">{p.points} 积分</Badge>
                      <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>库存 {p.stock ?? 0}</span>
                    </div>
                    {p.description && (
                      <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>{p.description}</p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}