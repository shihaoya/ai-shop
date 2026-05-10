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
      <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)' }}>消息中心</h1>
      {loading ? (
        <div className="space-y-4">
          {[1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="glass-skeleton h-20" />)}
        </div>
      ) : messages.length === 0 ? (
        <Card glass>
          <CardContent style={{ textAlign: 'center', padding: '48px' }}>
            <Mail style={{ width: '48px', height: '48px', margin: '0 auto 16px', opacity: 0.5 }} />
            <p style={{ color: 'var(--text-muted)' }}>暂无消息</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {messages.map((msg) => (
            <Card glass key={msg.id}>
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="space-y-1">
                    <p style={{ fontWeight: 500, color: 'var(--text-primary)' }}>{msg.title || '系统消息'}</p>
                    <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>{msg.content}</p>
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