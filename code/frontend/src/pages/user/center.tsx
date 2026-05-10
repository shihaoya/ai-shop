import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Coins, ArrowUp, ArrowDown, History, MapPin, MessageSquare, User } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from 'sonner'
import { getPoints, getPointsLog, type PointsLog } from '@/services/user'
import { useAuthStore } from '@/stores/auth'

const TYPE_MAP: Record<number, { label: string; color: string }> = {
  1: { label: '收入', color: 'text-green-600' },
  2: { label: '支出', color: 'text-red-600' },
}

export default function CenterPage() {
  const { userInfo } = useAuthStore()
  const [pointsLoading, setPointsLoading] = useState(true)
  const [logLoading, setLogLoading] = useState(true)
  const [points, setPoints] = useState(0)
  const [logs, setLogs] = useState<PointsLog[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize] = useState(10)

  useEffect(() => {
    const fetchPoints = async () => {
      try {
        const res = await getPoints()
        setPoints(res.points)
      } catch (err: any) {
        toast.error(err.message || '获取积分失败')
      } finally {
        setPointsLoading(false)
      }
    }
    fetchPoints()
  }, [])

  useEffect(() => {
    const fetchLogs = async () => {
      setLogLoading(true)
      try {
        const res = await getPointsLog({ page, pageSize })
        setLogs(res.list)
        setTotal(res.total)
      } catch (err: any) {
        toast.error(err.message || '获取积分流水失败')
      } finally {
        setLogLoading(false)
      }
    }
    fetchLogs()
  }, [page])

  const totalPages = Math.ceil(total / pageSize)

  return (
    <div className="max-w-2xl mx-auto p-4 space-y-6">
      {/* User Info Card */}
      <Card className="glass-card">
        <CardContent className="p-6">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-full bg-accent/10 flex items-center justify-center">
              <User className="w-8 h-8 text-accent" />
            </div>
            <div>
              <h2 className="text-lg font-medium">{userInfo?.nickname || userInfo?.username || '用户'}</h2>
              <p className="text-sm text-muted-foreground">普通用户</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Points Balance Card */}
      <Card className="glass-card">
        <CardHeader className="pb-3">
          <div className="flex items-center gap-2">
            <Coins className="w-5 h-5 text-accent" />
            <CardTitle>积分余额</CardTitle>
          </div>
        </CardHeader>
        <CardContent>
          {pointsLoading ? (
            <Skeleton className="h-10 w-32 glass-skeleton" />
          ) : (
            <div className="flex items-center gap-3">
              <div className="text-3xl font-bold text-accent">{points}</div>
              <span className="glass-badge px-2 py-1 rounded-full text-xs">积分</span>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <div className="grid grid-cols-3 gap-4">
        <Link to="/user/addresses">
          <Card className="glass-card hover:ring-2 hover:ring-accent/50 transition-all cursor-pointer">
            <CardContent className="p-4 flex flex-col items-center gap-2">
              <MapPin className="w-6 h-6 text-muted-foreground" />
              <span className="text-sm">地址管理</span>
            </CardContent>
          </Card>
        </Link>
        <Link to="/user/messages">
          <Card className="glass-card hover:ring-2 hover:ring-accent/50 transition-all cursor-pointer">
            <CardContent className="p-4 flex flex-col items-center gap-2">
              <MessageSquare className="w-6 h-6 text-muted-foreground" />
              <span className="text-sm">消息中心</span>
            </CardContent>
          </Card>
        </Link>
        <Link to="/user/orders">
          <Card className="glass-card hover:ring-2 hover:ring-accent/50 transition-all cursor-pointer">
            <CardContent className="p-4 flex flex-col items-center gap-2">
              <History className="w-6 h-6 text-muted-foreground" />
              <span className="text-sm">订单记录</span>
            </CardContent>
          </Card>
        </Link>
      </div>

      {/* Points Flow */}
      <Card className="glass-card">
        <CardHeader className="pb-3">
          <div className="flex items-center gap-2">
            <History className="w-5 h-5 text-muted-foreground" />
            <CardTitle>积分流水</CardTitle>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {logLoading ? (
            <div className="space-y-3">
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className="h-12 w-full glass-skeleton" />
              ))}
            </div>
          ) : logs.length === 0 ? (
            <p className="text-center py-6 text-muted-foreground">暂无积分记录</p>
          ) : (
            <>
              <div className="space-y-3">
                {logs.map((log) => (
                  <div key={log.id} className="glass-card flex items-center justify-between p-3 rounded-lg">
                    <div className="flex items-center gap-3">
                      <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                        log.type === 1 ? 'bg-green-100/50' : 'bg-red-100/50'
                      }`}>
                        {log.type === 1 ? (
                          <ArrowUp className="w-4 h-4 text-green-600" />
                        ) : (
                          <ArrowDown className="w-4 h-4 text-red-600" />
                        )}
                      </div>
                      <div>
                        <p className="text-sm font-medium">{log.description}</p>
                        <p className="text-xs text-muted-foreground">
                          {new Date(log.createTime).toLocaleString()}
                        </p>
                      </div>
                    </div>
                    <span className={`font-semibold ${TYPE_MAP[log.type]?.color || ''}`}>
                      {log.type === 1 ? '+' : '-'}{log.points}
                    </span>
                  </div>
                ))}
              </div>
              {totalPages > 1 && (
                <div className="flex justify-center gap-2 pt-4">
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={page === 1}
                    onClick={() => setPage(page - 1)}
                    className="glass-btn"
                  >
                    上一页
                  </Button>
                  <span className="px-3 py-2 text-sm glass">
                    {page} / {totalPages}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={page === totalPages}
                    onClick={() => setPage(page + 1)}
                    className="glass-btn"
                  >
                    下一页
                  </Button>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}