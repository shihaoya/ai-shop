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
      <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)' }}>用户管理</h1>
      <Card glass>
        <CardContent className="p-0">
          {loading ? (
            <div className="p-4 space-y-2">
              {[1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="glass-skeleton h-12" />)}
            </div>
          ) : users.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '48px', color: 'var(--text-muted)' }}>
              <Users style={{ width: '48px', height: '48px', margin: '0 auto 16px', opacity: 0.5 }} />
              <p style={{ color: 'var(--text-muted)' }}>暂无用户</p>
            </div>
          ) : (
            <Table className="glass-table">
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
                    <TableCell><Badge className="glass-badge">{statusMap[u.status] || '-'}</Badge></TableCell>
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