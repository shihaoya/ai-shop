import {
  Activity, BarChart3, DollarSign, Download, Eye,
  Plus, ShoppingCart, TrendingUp, UserCheck, UserPlus,
  Users, XCircle
} from 'lucide-react'
import { useState } from 'react'

interface Order {
  id: string
  user: string
  email: string
  product: string
  amount: string
  status: 'active' | 'pending' | 'inactive'
  statusText: string
}

interface ActivityItem {
  icon: 'UserPlus' | 'ShoppingCart' | 'UserCheck' | 'XCircle'
  title: string
  desc: string
  extra: string
  extraColor?: 'green' | 'default'
  time: string
}

export default function AdminDashboard() {
  const [selectedPeriod] = useState('7days')

  const stats = [
    { icon: Users, label: '总用户数', value: '0', trend: 'up', trendValue: '12.5%' },
    { icon: ShoppingCart, label: '总订单数', value: '0', trend: 'up', trendValue: '8.2%' },
    { icon: DollarSign, label: '总交易额', value: '¥0', trend: 'down', trendValue: '3.1%' },
    { icon: TrendingUp, label: '今日收入', value: '¥0', trend: 'up', trendValue: '15.3%' },
  ]

  const orders: Order[] = [
    { id: '#202401011234', user: '李明', email: 'liming@example.com', product: '无线蓝牙耳机', amount: '¥299', status: 'active', statusText: '已完成' },
    { id: '#202401011233', user: '王芳', email: 'wangfang@example.com', product: '机械键盘', amount: '¥599', status: 'pending', statusText: '处理中' },
    { id: '#202401011232', user: '张伟', email: 'zhangwei@example.com', product: '智能手表', amount: '¥1,299', status: 'active', statusText: '已完成' },
    { id: '#202401011231', user: '刘洋', email: 'liuyang@example.com', product: '移动电源', amount: '¥159', status: 'inactive', statusText: '已取消' },
  ]

  const activities: ActivityItem[] = [
    { icon: 'UserPlus', title: '新用户注册', desc: '王芳 刚刚注册成功', extra: '刚刚', time: '刚刚' },
    { icon: 'ShoppingCart', title: '新订单', desc: '用户李明购买了无线蓝牙耳机', extra: '+¥299', extraColor: 'green', time: '+¥299' },
    { icon: 'UserCheck', title: '用户升级', desc: '张伟 升级为VIP用户', extra: '5分钟前', time: '5分钟前' },
    { icon: 'XCircle', title: '订单取消', desc: '刘洋 取消了订单 #202401011231', extra: '15分钟前', time: '15分钟前' },
  ]

  const barHeights = [60, 80, 50, 70, 90, 75, 100]
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

  const ActivityIcon = ({ type }: { type: ActivityItem['icon'] }) => {
    const props = { size: 20 }
    switch (type) {
      case 'UserPlus': return <UserPlus {...props} />
      case 'ShoppingCart': return <ShoppingCart {...props} />
      case 'UserCheck': return <UserCheck {...props} />
      case 'XCircle': return <XCircle {...props} />
    }
  }

  return (
    <div style={{ padding: '24px' }}>
      {/* Page Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '32px' }}>
        <div>
          <h1 style={{ fontSize: '28px', fontWeight: 700, color: 'var(--text-primary)' }}>工作台</h1>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '4px' }}>欢迎回来！</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button type="button" className="btn-secondary">
            <Download size={16} style={{ marginRight: '6px' }} />
            导出数据
          </button>
          <button type="button" className="btn-primary">
            <Plus size={16} style={{ marginRight: '6px' }} />
            新建商品
          </button>
        </div>
      </div>

      {/* Stats Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '24px' }}>
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
              <div style={{ fontSize: '32px', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '4px' }}>
                {stat.value}
              </div>
              <div style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                {stat.label}
              </div>
            </div>
          )
        })}
      </div>

      {/* Content Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '20px' }}>
        {/* Left - Recent Orders */}
        <div className="glass-card" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <ShoppingCart size={20} style={{ color: 'var(--accent)' }} />
              <span style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-primary)' }}>最新订单</span>
            </div>
            <button type="button" style={{ fontSize: '13px', color: 'var(--accent)', background: 'none', border: 'none', cursor: 'pointer' }}>
              查看全部
            </button>
          </div>

          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={thStyle}>订单号</th>
                <th style={thStyle}>用户</th>
                <th style={thStyle}>商品</th>
                <th style={thStyle}>金额</th>
                <th style={thStyle}>状态</th>
                <th style={thStyle}>操作</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => {
                const rowKey = `order-${order.id}`
                return (
                  <tr key={rowKey} style={{ transition: 'background 0.2s' }}
                      onMouseEnter={e => (e.currentTarget.style.background = 'rgba(59,130,246,0.08)')}
                      onMouseLeave={e => (e.currentTarget.style.background = 'transparent')}>
                    <td style={{ ...tdStyle, color: 'var(--accent)', fontWeight: 500 }}>{order.id}</td>
                    <td style={tdStyle}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{
                          width: '32px', height: '32px', borderRadius: '50%',
                          background: 'var(--accent-light)', display: 'flex',
                          alignItems: 'center', justifyContent: 'center',
                          fontSize: '12px', fontWeight: 600, color: 'var(--accent)'
                        }}>
                          {order.user[0]}
                        </div>
                        <div>
                          <div style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text-primary)' }}>{order.user}</div>
                          <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{order.email}</div>
                        </div>
                      </div>
                    </td>
                    <td style={{ ...tdStyle, color: 'var(--text-secondary)' }}>{order.product}</td>
                    <td style={{ ...tdStyle, fontWeight: 600, color: 'var(--text-primary)' }}>{order.amount}</td>
                    <td style={tdStyle}>
                      <span className={`status-badge ${order.status}`}>
                        {order.statusText}
                      </span>
                    </td>
                    <td style={tdStyle}>
                      <button type="button" className="action-btn">
                        <Eye size={16} style={{ color: 'var(--text-muted)' }} />
                      </button>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>

        {/* Right Column */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Sales Trend */}
          <div className="glass-card" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <BarChart3 size={20} style={{ color: 'var(--accent)' }} />
                <span style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-primary)' }}>销售趋势</span>
              </div>
              <select
                value={selectedPeriod}
                onChange={() => {}}
                style={{
                  background: 'var(--card-bg)', border: '1px solid var(--card-border)',
                  borderRadius: '8px', padding: '6px 12px', fontSize: '13px',
                  color: 'var(--text-secondary)', cursor: 'pointer'
                }}
              >
                <option value="7days">近7天</option>
                <option value="30days">近30天</option>
              </select>
            </div>

            <div style={{ display: 'flex', alignItems: 'flex-end', gap: '8px', height: '120px' }}>
              {barHeights.map((height, idx) => {
                const barKey = `bar-${days[idx]}`
                return (
                  <div key={barKey} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
                    <div style={{
                      width: '100%', height: `${height}%`,
                      background: 'linear-gradient(to top, var(--accent), var(--accent-glow))',
                      borderRadius: '6px 6px 0 0',
                      minHeight: '8px'
                    }} />
                    <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{days[idx]}</span>
                  </div>
                )
              })}
            </div>
          </div>

          {/* Recent Activity */}
          <div className="glass-card" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
              <Activity size={20} style={{ color: 'var(--accent)' }} />
              <span style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-primary)' }}>近期活动</span>
            </div>

            <div>
              {activities.map((item, idx) => {
                const iconBg: Record<ActivityItem['icon'], string> = {
                  UserPlus: 'rgba(59,130,246,0.15)',
                  ShoppingCart: 'rgba(16,185,129,0.15)',
                  UserCheck: 'rgba(139,92,246,0.15)',
                  XCircle: 'rgba(244,63,94,0.15)',
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
                        color: item.extraColor === 'green' ? '#10b981' : 'var(--text-muted)'
                      }}>
                        {item.extra}
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        </div>
      </div>

      <style>{`
        .btn-primary {
          display: inline-flex;
          align-items: center;
          background: var(--accent);
          color: white;
          border: none;
          border-radius: 12px;
          padding: 10px 18px;
          font-size: 14px;
          font-weight: 500;
          cursor: pointer;
          box-shadow: 0 4px 16px var(--accent-glow);
          transition: all 0.2s;
        }
        .btn-primary:hover {
          opacity: 0.9;
          transform: translateY(-1px);
        }
        .btn-secondary {
          display: inline-flex;
          align-items: center;
          background: var(--card-bg);
          color: var(--text-primary);
          border: 1px solid var(--card-border);
          border-radius: 12px;
          padding: 10px 18px;
          font-size: 14px;
          font-weight: 500;
          cursor: pointer;
          transition: all 0.2s;
        }
        .btn-secondary:hover {
          border-color: var(--accent);
        }
        .stat-card {
          background: var(--card-bg);
          border: 1px solid var(--card-border);
          border-radius: 20px;
          padding: 24px;
          transition: all 0.3s;
        }
        .stat-card:hover {
          border-color: var(--accent);
          transform: translateY(-4px);
          box-shadow: 0 8px 32px var(--accent-glow);
        }
        .glass-card {
          background: var(--card-bg);
          border: 1px solid var(--card-border);
          border-radius: 20px;
        }
        .trend-badge {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          font-weight: 600;
          padding: 4px 10px;
          border-radius: 8px;
        }
        .trend-badge.up {
          background: rgba(16,185,129,0.15);
          color: #10b981;
        }
        .trend-badge.down {
          background: rgba(244,63,94,0.15);
          color: #f43f5e;
        }
        .status-badge {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          padding: 5px 12px;
          border-radius: 20px;
          font-size: 12px;
          font-weight: 500;
        }
        .status-badge::before {
          content: '';
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: currentColor;
        }
        .status-badge.active {
          background: rgba(16,185,129,0.15);
          color: #10b981;
        }
        .status-badge.pending {
          background: rgba(245,158,11,0.15);
          color: #f59e0b;
        }
        .status-badge.inactive {
          background: rgba(100,116,139,0.15);
          color: var(--text-muted);
        }
        .action-btn {
          width: 36px;
          height: 36px;
          border-radius: 12px;
          border: 1px solid var(--card-border);
          background: var(--glass-bg);
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          transition: all 0.2s;
        }
        .action-btn:hover {
          border-color: var(--accent);
          background: var(--accent-light);
        }
      `}</style>
    </div>
  )
}

const thStyle: React.CSSProperties = {
  fontSize: '11px',
  textTransform: 'uppercase',
  color: 'var(--text-muted)',
  background: 'rgba(30,41,59,0.3)',
  borderBottom: '1px solid var(--card-border)',
  padding: '12px 16px',
  textAlign: 'left',
  fontWeight: 600,
}

const tdStyle: React.CSSProperties = {
  padding: '16px',
  fontSize: '14px',
  borderBottom: '1px solid var(--card-border)',
}