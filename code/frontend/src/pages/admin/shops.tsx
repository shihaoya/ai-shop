import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { getShops, auditShop } from '@/services/admin'
import { toast } from 'sonner'

export default function AdminShops() {
  const [shops, setShops] = useState<any[]>([])
  const [params] = useSearchParams({ page: '1', keyword: '' })

  const loadData = async () => {
    try {
      const res = await getShops({ page: Number(params.get('page')), keyword: params.get('keyword') || '' })
      setShops(res.list || [])
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  const handleAudit = async (id: string, status: number) => {
    try {
      await auditShop(id, status)
      toast.success(status === 2 ? '已通过' : '已拒绝')
      loadData()
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  const statusMap: Record<number, string> = { 1: '待审核', 2: '已通过', 3: '已拒绝', 4: '已禁用' }
  const statusColor: Record<number, string> = { 1: 'bg-yellow-500', 2: 'bg-green-500', 3: 'bg-red-500', 4: 'bg-gray-500' }

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">店铺管理</h1>
      <Card className="glass-card">
        <CardHeader>
          <div className="flex items-center gap-4">
            <Input placeholder="搜索店铺名称" className="glass-input w-64" />
            <Button className="glass-btn">搜索</Button>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="glass-table">ID</TableHead>
                <TableHead className="glass-table">店铺名称</TableHead>
                <TableHead className="glass-table">运营者</TableHead>
                <TableHead className="glass-table">状态</TableHead>
                <TableHead className="glass-table">申请时间</TableHead>
                <TableHead className="glass-table">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {shops.map((shop) => (
                <TableRow key={shop.id}>
                  <TableCell>{shop.id}</TableCell>
                  <TableCell>{shop.name}</TableCell>
                  <TableCell>{shop.operatorUsername}</TableCell>
                  <TableCell>
                    <Badge className={`glass-badge ${statusColor[shop.status]}`}>{statusMap[shop.status]}</Badge>
                  </TableCell>
                  <TableCell>{shop.createdAt}</TableCell>
                  <TableCell>
                    {shop.status === 1 && (
                      <>
                        <Button size="sm" className="glass-btn mr-2" onClick={() => handleAudit(shop.id, 2)}>通过</Button>
                        <Button size="sm" className="glass-btn" onClick={() => handleAudit(shop.id, 3)}>拒绝</Button>
                      </>
                    )}
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