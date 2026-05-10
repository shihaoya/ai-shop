import { useState, useEffect } from 'react'
import { Copy, RefreshCw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { getInviteCode, generateInviteCode } from '@/services/admin'
import { toast } from 'sonner'

export default function AdminInviteCode() {
  const [code, setCode] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    loadCode()
  }, [])

  const loadCode = async () => {
    setLoading(true)
    try {
      const res = await getInviteCode()
      setCode(res.code || '')
    } catch (err: any) {
      // 忽略错误，可能还没有邀请码
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = async () => {
    setLoading(true)
    try {
      const res = await generateInviteCode()
      setCode(res.code || '')
      toast.success('邀请码已生成')
    } catch (err: any) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleCopy = () => {
    navigator.clipboard.writeText(code)
    toast.success('已复制')
  }

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">邀请码管理</h1>
      <Card className="glass-card">
        <CardHeader>
          <CardTitle>我的邀请码</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center gap-4">
            <Input value={code} readOnly placeholder="点击生成获取邀请码" className="glass-input w-64" />
            <Button variant="outline" size="icon" className="glass-btn" onClick={handleCopy}>
              <Copy className="h-4 w-4" />
            </Button>
            <Button className="glass-btn-primary" onClick={handleCreate} disabled={loading}>
              <RefreshCw className="h-4 w-4 mr-2" />
              {loading ? '生成中...' : '生成新邀请码'}
            </Button>
          </div>
          <p className="text-sm text-neutral-500">重新生成会使旧邀请码失效</p>
        </CardContent>
      </Card>
    </div>
  )
}