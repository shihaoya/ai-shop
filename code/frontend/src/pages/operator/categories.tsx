import { useState } from 'react'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/services/operator'
import { toast } from 'sonner'

export default function OperatorCategories() {
  const [categories, setCategories] = useState<any[]>([])
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ id: '', name: '', sort: '' })

  const loadData = async () => {
    try {
      const res = await getCategories()
      setCategories(res || [])
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  const handleSubmit = async () => {
    try {
      if (form.id) {
        await updateCategory(form.id, { name: form.name })
        toast.success('已更新')
      } else {
        await createCategory({ name: form.name })
        toast.success('已创建')
      }
      setShowForm(false)
      setForm({ id: '', name: '', sort: '' })
      loadData()
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  const handleDelete = async (id: string) => {
    if (!confirm('确定删除?')) return
    try {
      await deleteCategory(id)
      toast.success('已删除')
      loadData()
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center justify-between">
        <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)' }}>分类管理</h1>
        <Button className="glass-btn-primary" onClick={() => setShowForm(true)}><Plus className="h-4 w-4 mr-2" /> 添加分类</Button>
      </div>

      {showForm && (
        <Card glass>
          <CardHeader><CardTitle>{form.id ? '编辑' : '新建'}分类</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label style={{ color: 'var(--text-primary)', fontSize: '14px', fontWeight: 500 }}>名称</Label>
                <Input className="glass-input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
              </div>
              <div className="space-y-2">
                <Label style={{ color: 'var(--text-primary)', fontSize: '14px', fontWeight: 500 }}>排序</Label>
                <Input className="glass-input" type="number" value={form.sort} onChange={(e) => setForm({ ...form, sort: e.target.value })} />
              </div>
            </div>
            <div className="flex gap-2">
              <Button className="glass-btn-primary" onClick={handleSubmit}>
                {form.id ? '更新' : '创建'}
              </Button>
              <Button className="glass-btn" variant="outline" onClick={() => { setShowForm(false); setForm({ id: '', name: '', sort: '' }) }}>
                取消
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      <Card glass>
        <CardContent className="p-0">
          <Table className="glass-table">
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead>
                <TableHead>名称</TableHead>
                <TableHead>排序</TableHead>
                <TableHead>操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {categories.map((cat) => (
                <TableRow key={cat.id}>
                  <TableCell className="font-mono text-xs">{cat.id}</TableCell>
                  <TableCell>{cat.name}</TableCell>
                  <TableCell>{cat.sort ?? 0}</TableCell>
                  <TableCell>
                    <div className="flex gap-2">
                      <Button size="sm" variant="ghost" className="action-btn" onClick={() => { setForm({ id: cat.id, name: cat.name, sort: String(cat.sort ?? '') }); setShowForm(true) }}>
                        <Pencil className="h-4 w-4" />
                      </Button>
                      <Button size="sm" variant="ghost" className="action-btn" style={{ color: '#ef4444' }} onClick={() => handleDelete(cat.id)}>
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  )
}