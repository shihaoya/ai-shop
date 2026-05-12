<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
})

const searchQuery = ref('')
const roleFilter = ref('')
const statusFilter = ref('')

const users = ref([
  {
    id: '1894-56-217842',
    username: 'zhangsan',
    nickname: '张三',
    role: 'shop',
    status: '正常',
    points: '12,580',
    registerTime: '2026-03-12',
  },
  {
    id: '1894-56-217843',
    username: 'lisi',
    nickname: '李四',
    role: 'user',
    status: '正常',
    points: '3,240',
    registerTime: '2026-04-05',
  },
  {
    id: '1894-56-217844',
    username: 'wangwu',
    nickname: '王五',
    role: 'user',
    status: '待审核',
    points: '0',
    registerTime: '2026-04-08',
  },
  {
    id: '1894-56-217845',
    username: 'zhaoliu',
    nickname: '赵六',
    role: 'shop',
    status: '已冻结',
    points: '8,120',
    registerTime: '2026-02-20',
  },
  {
    id: '1894-56-217846',
    username: 'sunqi',
    nickname: '孙七',
    role: 'user',
    status: '正常',
    points: '1,560',
    registerTime: '2026-04-10',
  },
])

function handleSearch() {
  // TODO: implement search
}

function handleReset() {
  searchQuery.value = ''
  roleFilter.value = ''
  statusFilter.value = ''
}
</script>

<template>
  <div id="page-user-manage">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <main class="main-content">
      <!-- Page Head -->
      <div class="page-head">
        <h2><span class="accent-line"></span>用户管理</h2>
        <div class="actions">
          <button class="cyber-btn">
            <i class="fas fa-download" style="margin-right:5px;"></i>导出
          </button>
          <button class="cyber-btn-primary">
            <i class="fas fa-plus" style="margin-right:5px;"></i>新增用户
          </button>
        </div>
      </div>

      <!-- Search Bar -->
      <div class="search-bar">
        <div class="inner">
          <input
            v-model="searchQuery"
            class="cyber-input"
            type="text"
            placeholder="搜索用户名、昵称..."
            style="flex:1;max-width:220px;"
          />
          <select
            v-model="roleFilter"
            class="cyber-input"
            style="max-width:130px;cursor:pointer;"
          >
            <option value="">全部角色</option>
            <option value="shop">店铺用户</option>
            <option value="user">普通用户</option>
          </select>
          <select
            v-model="statusFilter"
            class="cyber-input"
            style="max-width:130px;cursor:pointer;"
          >
            <option value="">全部状态</option>
            <option value="正常">正常</option>
            <option value="待审核">待审核</option>
            <option value="已冻结">已冻结</option>
          </select>
          <button class="cyber-btn-primary" style="padding:9px 16px;" @click="handleSearch">
            <i class="fas fa-search" style="margin-right:5px;"></i>搜索
          </button>
          <button class="cyber-btn" style="padding:9px 16px;" @click="handleReset">
            <i class="fas fa-undo"></i>
          </button>
        </div>
      </div>

      <!-- Table Card -->
      <div class="table-card">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th style="width:36px;"><input type="checkbox" /></th>
                <th>ID</th>
                <th>用户名</th>
                <th>昵称</th>
                <th>角色</th>
                <th>状态</th>
                <th>积分</th>
                <th>注册时间</th>
                <th style="width:85px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td><input type="checkbox" /></td>
                <td class="id-cell">{{ user.id }}</td>
                <td><strong>{{ user.username }}</strong></td>
                <td>{{ user.nickname }}</td>
                <td>
                  <span v-if="user.role === 'shop'" class="role-tag shop">
                    <i class="fas fa-store"></i>店铺
                  </span>
                  <span v-else class="role-tag user">
                    <i class="fas fa-user"></i>普通
                  </span>
                </td>
                <td>
                  <span
                    class="status-tag"
                    :class="{
                      green: user.status === '正常',
                      orange: user.status === '待审核',
                      red: user.status === '已冻结',
                    }"
                  >
                    <span class="dot"></span>{{ user.status }}
                  </span>
                </td>
                <td>
                  <strong class="points-cell">{{ user.points }}</strong>
                </td>
                <td class="time-cell">{{ user.registerTime }}</td>
                <td>
                  <button class="action-btn" title="编辑">
                    <i class="fas fa-edit"></i>
                  </button>
                  <button class="action-btn" title="更多">
                    <i class="fas fa-ellipsis-v"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination">
        <span># TOTAL: 42 RECORDS</span>
        <button class="page-btn">
          <i class="fas fa-chevron-left" style="font-size:10px;"></i>
        </button>
        <button class="page-btn active">1</button>
        <button class="page-btn">2</button>
        <button class="page-btn">3</button>
        <button class="page-btn">...</button>
        <button class="page-btn">5</button>
        <button class="page-btn">
          <i class="fas fa-chevron-right" style="font-size:10px;"></i>
        </button>
      </div>
    </main>
  </div>
</template>

<style scoped>
#page-user-manage {
  min-height: 100vh;
  position: relative;
}

.id-cell {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
}

.points-cell {
  color: var(--accent-light);
  text-shadow: var(--accent-glow-text);
}

.time-cell {
  font-size: 12px;
  color: var(--text-muted);
}
</style>