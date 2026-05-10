import { useState, useEffect } from 'react'
import { MapPin, Plus, Edit2, Trash2, Check, Loader2 } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from 'sonner'
import { getAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress, type Address } from '@/services/user'

export default function AddressesPage() {
  const [loading, setLoading] = useState(true)
  const [addresses, setAddresses] = useState<Address[]>([])
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [form, setForm] = useState({
    name: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: 0,
  })

  const fetchAddresses = async () => {
    setLoading(true)
    try {
      const res = await getAddresses()
      setAddresses(res)
    } catch (err: any) {
      toast.error(err.message || '获取地址列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchAddresses()
  }, [])

  const resetForm = () => {
    setForm({
      name: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      detail: '',
      isDefault: 0,
    })
    setEditingId(null)
    setShowForm(false)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      if (editingId) {
        await updateAddress(editingId, form)
        toast.success('地址更新成功')
      } else {
        await createAddress(form)
        toast.success('地址添加成功')
      }
      resetForm()
      fetchAddresses()
    } catch (err: any) {
      toast.error(err.message || '操作失败')
    } finally {
      setSubmitting(false)
    }
  }

  const handleEdit = (addr: Address) => {
    setForm({
      name: addr.name,
      phone: addr.phone,
      province: addr.province,
      city: addr.city,
      district: addr.district,
      detail: addr.detail,
      isDefault: addr.isDefault,
    })
    setEditingId(addr.id)
    setShowForm(true)
  }

  const handleDelete = async (id: string) => {
    if (!confirm('确定要删除该地址吗？')) return
    try {
      await deleteAddress(id)
      toast.success('地址已删除')
      fetchAddresses()
    } catch (err: any) {
      toast.error(err.message || '删除失败')
    }
  }

  const handleSetDefault = async (id: string) => {
    try {
      await setDefaultAddress(id)
      toast.success('已设为默认地址')
      fetchAddresses()
    } catch (err: any) {
      toast.error(err.message || '设置失败')
    }
  }

  return (
    <div className="max-w-2xl mx-auto p-4 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold">收货地址</h1>
        {!showForm && (
          <Button
            size="sm"
            className="gap-2"
            onClick={() => setShowForm(true)}
          >
            <Plus className="w-4 h-4" />
            新增地址
          </Button>
        )}
      </div>

      {/* Address Form */}
      {showForm && (
        <Card>
          <CardHeader className="pb-4">
            <CardTitle>{editingId ? '编辑地址' : '新增地址'}</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="name">收货人</Label>
                  <Input
                    id="name"
                    value={form.name}
                    onChange={(e) => setForm({ ...form, name: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">联系电话</Label>
                  <Input
                    id="phone"
                    value={form.phone}
                    onChange={(e) => setForm({ ...form, phone: e.target.value })}
                    required
                  />
                </div>
              </div>
              <div className="grid grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="province">省份</Label>
                  <Input
                    id="province"
                    value={form.province}
                    onChange={(e) => setForm({ ...form, province: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="city">城市</Label>
                  <Input
                    id="city"
                    value={form.city}
                    onChange={(e) => setForm({ ...form, city: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="district">区县</Label>
                  <Input
                    id="district"
                    value={form.district}
                    onChange={(e) => setForm({ ...form, district: e.target.value })}
                    required
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="detail">详细地址</Label>
                <Input
                  id="detail"
                  value={form.detail}
                  onChange={(e) => setForm({ ...form, detail: e.target.value })}
                  required
                />
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="isDefault"
                  checked={form.isDefault === 1}
                  onChange={(e) => setForm({ ...form, isDefault: e.target.checked ? 1 : 0 })}
                  className="rounded"
                />
                <Label htmlFor="isDefault">设为默认地址</Label>
              </div>
              <div className="flex gap-3 pt-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={resetForm}
                >
                  取消
                </Button>
                <Button type="submit" disabled={submitting}>
                  {submitting && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                  {editingId ? '保存' : '添加'}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {/* Address List */}
      {loading ? (
        <div className="space-y-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-24 w-full" />
          ))}
        </div>
      ) : addresses.length === 0 ? (
        <div className="text-center py-12 text-muted-foreground">
          <MapPin className="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>暂无收货地址</p>
        </div>
      ) : (
        <div className="space-y-4">
          {addresses.map((addr) => (
            <Card key={addr.id}>
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="font-medium">{addr.name}</span>
                      <span className="text-sm text-muted-foreground">{addr.phone}</span>
                      {addr.isDefault === 1 && (
                        <span className="px-2 py-0.5 text-xs bg-accent/10 text-accent rounded">默认</span>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground mt-2">
                      {addr.province}{addr.city}{addr.district}{addr.detail}
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleEdit(addr)}
                    >
                      <Edit2 className="w-4 h-4" />
                    </Button>
                    {addr.isDefault !== 1 && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleSetDefault(addr.id)}
                      >
                        <Check className="w-4 h-4" />
                      </Button>
                    )}
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDelete(addr.id)}
                    >
                      <Trash2 className="w-4 h-4 text-destructive" />
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}