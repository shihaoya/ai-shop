import { useState } from 'react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { getUsers, updateUserStatus, approveUser } from '@/services/admin'
import { toast } from 'sonner'

export default function AdminUsers() {
  const [users, setUsers] = useState<any[]>([])

  const loadData = async () => {
    try {
      const res = await getUsers({ page: 1 })
      setUsers(res.list || [])
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  const handleStatus = async (id: string, status: number) => {
    try {
      await updateUserStatus(id, status)
      toast.success(status === 2 ? '已解冻' : '已冻结')
      loadData()
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  const handleApprove = async (id: string) => {
    try {
      await approveUser(id)
      toast.success('已审批通过')
      loadData()
    } catch (err: any) {
      toast.error(err.message)
    }
  }

  const roleMap: Record<number, string> = { 1: '管理员', 2: '店铺用户', 3: '普通用户' }
  const statusMap: Record<number, string> = { 1: '待审核', 2: '正常', 3: '已冻结' }

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">用户管理</h1>
      <Card>
        <CardHeader>
          <Input placeholder="搜索用户" className="w-64" />
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead>
                <TableHead>用户名</TableHead>
                <TableHead>昵称</TableHead>
                <TableHead>角色</TableHead>
                <TableHead>状态</TableHead>
                <TableHead>操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {users.map((user) => (
                <TableRow key={user.id}>
                  <TableCell>{user.id}</TableCell>
                  <TableCell>{user.username}</TableCell>
                  <TableCell>{user.nickname}</TableCell>
                  <TableCell>{roleMap[user.role]}</TableCell>
                  <TableCell><Badge>{statusMap[user.status]}</Badge></TableCell>
                  <TableCell>
                    {user.status === 1 && (
                      <Button size="sm" onClick={() => handleApprove(user.id)}>审批</Button>
                    )}
                    {user.status === 2 && (
                      <Button size="sm" variant="destructive" onClick={() => handleStatus(user.id, 3)}>冻结</Button>
                    )}
                    {user.status === 3 && (
                      <Button size="sm" onClick={() => handleStatus(user.id, 2)}>解冻</Button>
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