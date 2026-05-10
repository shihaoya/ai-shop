import { useState, useEffect } from 'react'
import { Users } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from 'sonner'
import { getUsers, type OperatorUser } from '@/services/operator'

export default function OperatorUsers() {
  const [loading, setLoading] = useState(true)
  const [users, setUsers] = useState<OperatorUser[]>([])

  useEffect(() => { loadData() }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await getUsers()
      setUsers(res.list || [])
    } catch (err: any) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  const roleMap: Record<number, string> = { 1: '管理员', 2: '店铺用户', 3: '普通用户' }
  const statusMap: Record<number, string> = { 1: '待审核', 2: '正常', 3: '已冻结' }

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">用户管理</h1>
      <Card>
        <CardContent className="p-0">
          {loading ? (
            <div className="p-4 space-y-2">
              {[1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="h-12" />)}
            </div>
          ) : users.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground">
              <Users className="w-12 h-12 mx-auto mb-4 opacity-50" />
              <p>暂无用户</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>用户名</TableHead>
                  <TableHead>昵称</TableHead>
                  <TableHead>角色</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead>积分</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {users.map((u) => (
                  <TableRow key={u.id}>
                    <TableCell className="font-mono text-xs">{u.id}</TableCell>
                    <TableCell>{u.username}</TableCell>
                    <TableCell>{u.nickname}</TableCell>
                    <TableCell>{roleMap[u.role] || '-'}</TableCell>
                    <TableCell><Badge>{statusMap[u.status] || '-'}</Badge></TableCell>
                    <TableCell>{u.points ?? '-'}</TableCell>
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