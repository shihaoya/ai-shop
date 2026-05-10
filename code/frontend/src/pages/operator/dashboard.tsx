import { Activity, Building2, Package, ShoppingCart, Users } from 'lucide-react'

interface ActivityItem {
  icon: 'Building2' | 'Package' | 'ShoppingCart' | 'Users'
  title: string
  desc: string
  time: string
}

export default function OperatorDashboard() {
  const stats = [
    { icon: Building2, label: '我的店铺', value: '-', trend: 'up' as const, trendValue: '+12%' },
    { icon: Package, label: '商品数', value: '0', trend: 'up' as const, trendValue: '+8%' },
    { icon: ShoppingCart, label: '订单数', value: '0', trend: 'down' as const, trendValue: '-3%' },
    { icon: Users, label: '用户数', value: '0', trend: 'up' as const, trendValue: '+23%' },
  ]

  const activities: ActivityItem[] = [
    { icon: 'ShoppingCart', title: '新订单', desc: '用户张三购买了商品', time: '刚刚' },
    { icon: 'Package', title: '商品上架', desc: '店铺新增1件商品', time: '10分钟前' },
    { icon: 'Users', title: '新用户', desc: '新用户李四注册成功', time: '30分钟前' },
    { icon: 'Building2', title: '店铺更新', desc: '店铺信息已更新', time: '1小时前' },
  ]

  const ActivityIcon = ({ type }: { type: ActivityItem['icon'] }) => {
    const props = { size: 20 }
    switch (type) {
      case 'Building2': return <Building2 {...props} />
      case 'Package': return <Package {...props} />
      case 'ShoppingCart': return <ShoppingCart {...props} />
      case 'Users': return <Users {...props} />
    }
  }

  return (
    <div style={{ padding: '24px' }}>
      {/* Page Header */}
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)' }}>工作台</h1>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '4px' }}>欢迎回来，今天也是充满干劲的一天</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6" style={{ marginBottom: '24px' }}>
        {stats.map((stat, idx) => {
          const Icon = stat.icon
          const key = `stat-${idx}`
          return (
            <div key={key} className="stat-card">
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
                <div style={{
                  width: '48px', height: '48px', borderRadius: '50%',
                  background: 'var(--accent-light)', display: 'flex',
                  alignItems: 'center', justifyContent: 'center'
                }}>
                  <Icon size={22} style={{ color: 'var(--accent)' }} />
                </div>
                <span className={`trend-badge ${stat.trend}`}>
                  {stat.trend === 'up' ? '↑' : '↓'} {stat.trendValue}
                </span>
              </div>
              <div style={{ fontSize: '28px', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '4px' }}>
                {stat.value}
              </div>
              <div style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                {stat.label}
              </div>
            </div>
          )
        })}
      </div>

      {/* Recent Activity */}
      <div className="glass-card p-6">
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
          <Activity size={20} style={{ color: 'var(--accent)' }} />
          <span style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-primary)' }}>近期活动</span>
        </div>

        <div>
          {activities.map((item, idx) => {
            const iconBg: Record<ActivityItem['icon'], string> = {
              Building2: 'rgba(59,130,246,0.15)',
              Package: 'rgba(16,185,129,0.15)',
              ShoppingCart: 'rgba(139,92,246,0.15)',
              Users: 'rgba(244,63,94,0.15)',
            }
            const itemKey = `activity-${idx}-${item.title}`
            return (
              <div key={itemKey} style={{
                display: 'flex', gap: '14px', padding: '14px 0',
                borderBottom: idx < activities.length - 1 ? '1px solid var(--card-border)' : 'none'
              }}>
                <div style={{
                  width: '44px', height: '44px', borderRadius: '12px',
                  background: iconBg[item.icon], display: 'flex',
                  alignItems: 'center', justifyContent: 'center',
                  flexShrink: 0
                }}>
                  <ActivityIcon type={item.icon} />
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text-primary)', marginBottom: '2px' }}>
                    {item.title}
                  </div>
                  <div style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '4px' }}>
                    {item.desc}
                  </div>
                  <div style={{
                    fontSize: '12px', fontWeight: 500,
                    color: 'var(--text-muted)'
                  }}>
                    {item.time}
                  </div>
                </div>
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}>
                  <div style={{
                    width: '8px', height: '8px', borderRadius: '50%',
                    background: 'var(--accent)'
                  }} />
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
