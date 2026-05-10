import { useState, useEffect } from 'react'
import { Mail } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from 'sonner'

interface Message {
  id: string
  title: string
  content: string
  isRead: number
  createTime?: string
}

export default function OperatorMessages() {
  const [loading, setLoading] = useState(true)
  const [messages, setMessages] = useState<Message[]>([])

  useEffect(() => { loadData() }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      // TODO: 从API获取消息
      setMessages([])
    } catch (err: any) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">消息中心</h1>
      {loading ? (
        <div className="space-y-4">
          {[1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="glass-skeleton h-20" />)}
        </div>
      ) : messages.length === 0 ? (
        <Card glass>
          <CardContent className="text-center py-12 text-muted-foreground">
            <Mail className="w-12 h-12 mx-auto mb-4 opacity-50" />
            <p>暂无消息</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {messages.map((msg) => (
            <Card glass key={msg.id}>
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="space-y-1">
                    <p className="font-medium">{msg.title || '系统消息'}</p>
                    <p className="text-sm text-muted-foreground">{msg.content}</p>
                  </div>
                  {msg.isRead !== 1 && <Badge className="glass-badge">未读</Badge>}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}