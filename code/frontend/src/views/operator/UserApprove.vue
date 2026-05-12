<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getUsers, approveUser } from '@/api/operator'
import { rejectUser } from '@/api/admin'
import { message } from 'ant-design-vue'
import type { UserInfo } from '@/types/api'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadUsers()
})

const loading = ref(false)
const users = ref<UserInfo[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })

async function loadUsers() {
  loading.value = true
  try {
    const res = await getUsers({ page: pagination.value.page, size: pagination.value.size, status: 1 })
    users.value = res.list.map((u: UserInfo) => ({
      ...u,
      id: String(u.id)
    }))
    pagination.value.total = res.total
  } catch (e: any) {
    console.error('加载待审核用户列表失败:', e)
    throw e
  } finally {
    loading.value = false
  }
}

async function handleApprove(userId: string) {
  try {
    await approveUser(userId)
    message.success('已通过审核')
    loadUsers()
  } catch (e) {
    throw e
  }
}

async function handleReject(userId: string) {
  try {
    await rejectUser(userId)
    message.success('已拒绝')
    loadUsers()
  } catch (e) {
    throw e
  }
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<template>
  <div id="page-user-approve">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>用户审核</h2>
        <div class="actions">
          <button class="cyber-btn" @click="loadUsers">
            <i class="fas fa-sync-alt" style="margin-right:5px;"></i>刷新
          </button>
        </div>
      </div>

      <div class="table-card">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>用户ID</th>
                <th>用户名</th>
                <th>昵称</th>
                <th>注册时间</th>
                <th style="width:160px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td class="id-cell">{{ user.id }}</td>
                <td>{{ user.username }}</td>
                <td>{{ user.nickname }}</td>
                <td class="time-cell">{{ formatDate(user.createdAt) }}</td>
                <td>
                  <button class="action-btn green" title="通过" @click="handleApprove(user.id)">
                    <i class="fas fa-check"></i>
                  </button>
                  <button class="action-btn red" title="拒绝" @click="handleReject(user.id)">
                    <i class="fas fa-times"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="users.length === 0 && !loading">
                <td colspan="5" class="empty-cell">
                  <i class="fas fa-inbox" style="font-size:32px;opacity:0.3;"></i>
                  <p>暂无待审核用户</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="loading" class="loading-mask">
          <i class="fas fa-spinner fa-spin"></i>
        </div>
      </div>

      <div class="pagination" v-if="pagination.total > 0">
        <span># TOTAL: {{ pagination.total }} RECORDS</span>
        <button class="page-btn" :disabled="pagination.page <= 1" @click="pagination.page--; loadUsers()">
          <i class="fas fa-chevron-left"></i>
        </button>
        <button class="page-btn active">{{ pagination.page }}</button>
        <button class="page-btn" :disabled="pagination.page * pagination.size >= pagination.total" @click="pagination.page++; loadUsers()">
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.id-cell {
  font-family: 'Courier New', monospace;
  color: var(--accent);
}
.time-cell {
  color: var(--text-secondary);
  font-size: 13px;
}
.empty-cell {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-secondary);
}
.empty-cell i {
  font-size: 32px;
  opacity: 0.3;
  margin-bottom: 12px;
}
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.status-tag .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
.status-tag.green { background: rgba(16,185,129,0.15); color: #10b981; }
.status-tag.green .dot { background: #10b981; }
.status-tag.orange { background: rgba(245,158,11,0.15); color: #f59e0b; }
.status-tag.orange .dot { background: #f59e0b; }
.status-tag.red { background: rgba(239,68,68,0.15); color: #ef4444; }
.status-tag.red .dot { background: #ef4444; }
.status-tag.gray { background: rgba(107,114,128,0.15); color: #6b7280; }
.status-tag.gray .dot { background: #6b7280; }
</style>