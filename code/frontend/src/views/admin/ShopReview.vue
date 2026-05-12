<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getShops, auditShop } from '@/api/admin'
import { message } from 'ant-design-vue'
import type { Shop } from '@/types/api'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadShops()
})

const loading = ref(false)
const shops = ref<Shop[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })

async function loadShops() {
  loading.value = true
  try {
    const res = await getShops({ page: pagination.value.page, size: pagination.value.size })
    shops.value = res.records.map(s => ({
      ...s,
      id: String(s.id)
    }))
    pagination.value.total = res.total
  } catch (e: any) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function handleAudit(shopId: string, status: number) {
  try {
    await auditShop(shopId, status)
    message.success(status === 1 ? '已通过审核' : '已拒绝')
    loadShops()
  } catch (e: any) {
    message.error(e.message || '操作失败')
  }
}

function getStatusTag(status: number) {
  const map: Record<number, { text: string; class: string }> = {
    0: { text: '待审核', class: 'orange' },
    1: { text: '已通过', class: 'green' },
    2: { text: '已拒绝', class: 'red' },
  }
  return map[status] || { text: '未知', class: 'gray' }
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<template>
  <div id="page-shop-review">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <!-- Page Head -->
      <div class="page-head">
        <h2><span class="accent-line"></span>店铺审核</h2>
        <div class="actions">
          <button class="cyber-btn" @click="loadShops">
            <i class="fas fa-sync-alt" style="margin-right:5px;"></i>刷新
          </button>
        </div>
      </div>

      <!-- Table Card -->
      <div class="table-card">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>店铺名称</th>
                <th>负责人</th>
                <th>状态</th>
                <th>营业状态</th>
                <th>申请时间</th>
                <th style="width:120px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="shop in shops" :key="shop.id">
                <td class="id-cell">{{ shop.id }}</td>
                <td>
                  <strong>{{ shop.name }}</strong>
                  <p v-if="shop.description" class="desc">{{ shop.description }}</p>
                </td>
                <td>{{ shop.ownerName || '-' }}</td>
                <td>
                  <span class="status-tag" :class="getStatusTag(shop.status).class">
                    <span class="dot"></span>{{ getStatusTag(shop.status).text }}
                  </span>
                </td>
                <td>
                  <span class="status-tag" :class="shop.isActive ? 'green' : 'gray'">
                    <span class="dot"></span>{{ shop.isActive ? '营业中' : '歇业' }}
                  </span>
                </td>
                <td class="time-cell">{{ formatDate(shop.createdAt) }}</td>
                <td>
                  <template v-if="shop.status === 0">
                    <button class="action-btn green" title="通过" @click="handleAudit(shop.id, 1)">
                      <i class="fas fa-check"></i>
                    </button>
                    <button class="action-btn red" title="拒绝" @click="handleAudit(shop.id, 2)">
                      <i class="fas fa-times"></i>
                    </button>
                  </template>
                  <span v-else class="text-muted">已处理</span>
                </td>
              </tr>
              <tr v-if="shops.length === 0 && !loading">
                <td colspan="7" class="empty-cell">
                  <i class="fas fa-inbox" style="font-size:32px;opacity:0.3;"></i>
                  <p>暂无待审核店铺</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="loading-mask">
          <i class="fas fa-spinner fa-spin"></i>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination" v-if="pagination.total > 0">
        <span># TOTAL: {{ pagination.total }} RECORDS</span>
        <button class="page-btn" :disabled="pagination.page <= 1" @click="pagination.page--; loadShops()">
          <i class="fas fa-chevron-left"></i>
        </button>
        <button class="page-btn active">{{ pagination.page }}</button>
        <button class="page-btn" :disabled="pagination.page * pagination.size >= pagination.total" @click="pagination.page++; loadShops()">
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-shop-review {
  min-height: 100vh;
  position: relative;
}

.desc {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time-cell {
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

.id-cell {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
}

.empty-cell {
  text-align: center;
  padding: 40px !important;
  color: var(--text-muted);
}

.empty-cell i {
  display: block;
  margin-bottom: 8px;
}

.loading-mask {
  position: absolute;
  inset: 0;
  background: rgba(7, 8, 22, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--accent);
}

.action-btn.green {
  color: var(--green);
}

.action-btn.green:hover {
  background: rgba(16, 185, 129, 0.1);
}

.action-btn.red {
  color: var(--red);
}

.action-btn.red:hover {
  background: rgba(239, 68, 68, 0.1);
}

.text-muted {
  color: var(--text-muted);
  font-size: 12px;
}
</style>