import { useState, useEffect } from 'react'
import { MessageSquare, Mail, Loader2, CheckCircle } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from 'sonner'
import { getMessages, readMessage, type Message } from '@/services/user'

export default function MessagesPage() {
  const [loading, setLoading] = useState(true)
  const [messages, setMessages] = useState<Message[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize] = useState(10)
  const [readingId, setReadingId] = useState<string | null>(null)

  const fetchMessages = async () => {
    setLoading(true)
    try {
      const res = await getMessages({ page, pageSize })
      setMessages(res.list)
      setTotal(res.total)
    } catch (err: any) {
      toast.error(err.message || '获取消息列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchMessages()
  }, [page])

  const handleRead = async (id: string) => {
    const msg = messages.find((m) => m.id === id)
    if (msg?.isRead === 1) return

    setReadingId(id)
    try {
      await readMessage(id)
      setMessages((prev) =>
        prev.map((m) => (m.id === id ? { ...m, isRead: 1 } : m))
      )
    } catch (err: any) {
      toast.error(err.message || '标记已读失败')
    } finally {
      setReadingId(null)
    }
  }

  const totalPages = Math.ceil(total / pageSize)

  return (
    <div className="max-w-2xl mx-auto p-4 space-y-6">
      <div className="flex items-center gap-2">
        <MessageSquare className="w-5 h-5" />
        <h1 className="text-xl font-semibold">消息中心</h1>
      </div>

      {loading ? (
        <div className="space-y-4">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-20 w-full" />
          ))}
        </div>
      ) : messages.length === 0 ? (
        <div className="text-center py-12 text-muted-foreground">
          <Mail className="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>暂无消息</p>
        </div>
      ) : (
        <div className="space-y-4">
          {messages.map((msg) => (
            <Card
              key={msg.id}
              className={`cursor-pointer hover:bg-muted/30 transition-colors ${
                msg.isRead === 1 ? 'opacity-60' : ''
              }`}
              onClick={() => handleRead(msg.id)}
            >
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <h3 className="font-medium">{msg.title}</h3>
                      {msg.isRead === 1 ? (
                        <Badge variant="outline" className="text-xs">已读</Badge>
                      ) : (
                        <Badge variant="default" className="text-xs">未读</Badge>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground mt-2 line-clamp-2">
                      {msg.content}
                    </p>
                    <p className="text-xs text-muted-foreground mt-2">
                      {new Date(msg.createTime).toLocaleString()}
                    </p>
                  </div>
                  {msg.isRead !== 1 && readingId === msg.id && (
                    <Loader2 className="w-4 h-4 animate-spin text-muted-foreground" />
                  )}
                  {msg.isRead === 1 && (
                    <CheckCircle className="w-4 h-4 text-muted-foreground" />
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-2">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 1}
            onClick={() => setPage(page - 1)}
          >
            上一页
          </Button>
          <span className="px-3 py-2 text-sm">
            {page} / {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={page === totalPages}
            onClick={() => setPage(page + 1)}
          >
            下一页
          </Button>
        </div>
      )}
    </div>
  )
}