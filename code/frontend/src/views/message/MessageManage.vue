<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getMessages, markMessageRead } from '@/api/operator'
import type { Message } from '@/types/api'
import CyberPagination from '@/components/CyberPagination.vue'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadMessages()
})

const loading = ref(false)
const messages = ref<Message[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })

// 未读数量
const unreadCount = computed(() => messages.value.filter(m => m.isRead === 0).length)

async function loadMessages() {
  loading.value = true
  try {
    const res = await getMessages({
      page: pagination.value.page,
      size: pagination.value.size
    })
    messages.value = res.list.map((m: Message) => ({
      ...m,
      id: String(m.id)
    }))
    pagination.value.total = res.total
  } finally {
    loading.value = false
  }
}

async function handleMessageClick(msg: Message) {
  if (msg.isRead === 0) {
    await markMessageRead(msg.id)
    await loadMessages()
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadMessages()
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<template>
  <div id="page-message-manage">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>消息中心</h2>
        <div class="header-stats" v-if="unreadCount > 0">
          <span class="unread-badge">{{ unreadCount }} 未读</span>
        </div>
      </div>

      <div class="cyber-card">
        <div class="message-list" v-if="messages.length > 0">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-item"
            :class="{ unread: msg.isRead === 0 }"
            @click="handleMessageClick(msg)"
          >
            <div class="message-icon">
              <i :class="msg.isRead === 0 ? 'fas fa-envelope' : 'fas fa-envelope-open'"></i>
            </div>
            <div class="message-content">
              <div class="message-title">{{ msg.title }}</div>
              <div class="message-text">{{ msg.content }}</div>
              <div class="message-time">{{ formatDate(msg.createdAt) }}</div>
            </div>
            <div class="message-status">
              <span v-if="msg.isRead === 0" class="status-unread">未读</span>
              <span v-else class="status-read">已读</span>
            </div>
          </div>
        </div>

        <div v-else-if="!loading" class="empty-state">
          <i class="fas fa-inbox"></i>
          <p>暂无消息</p>
        </div>

        <div v-if="loading" class="loading-state">
          <i class="fas fa-spinner fa-spin"></i>
          <p>加载中...</p>
        </div>

        <div class="pagination-wrapper" v-if="pagination.total > 0">
          <CyberPagination
            v-model:current="pagination.page"
            v-model:pageSize="pagination.size"
            :total="pagination.total"
            @change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-message-manage {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.cyber-bg-grid {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(var(--border-subtle) 1px, transparent 1px),
    linear-gradient(90deg, var(--border-subtle) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
  z-index: 0;
}

.cyber-bg-orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  z-index: 0;
}

.page-content {
  position: relative;
  z-index: 1;
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-head h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
}

.accent-line {
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, var(--accent), var(--accent-light));
  border-radius: 2px;
}

.header-stats {
  display: flex;
  gap: 16px;
}

.unread-badge {
  padding: 6px 16px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-subtle);
  border-radius: 20px;
  color: var(--red);
  font-size: 14px;
  font-weight: 500;
}

.cyber-card {
  position: relative;
  overflow: visible;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: 16px;
  padding: 24px;
}

.cyber-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--accent), transparent);
  opacity: 0.4;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-secondary);
  cursor: pointer;
  transition: all 0.3s;
}

.message-item:hover {
  border-color: var(--accent);
  box-shadow: var(--accent-glow);
  transform: translateY(-2px);
}

.message-item.unread {
  border-left: 3px solid var(--accent);
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-card));
}

.message-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  flex-shrink: 0;
}

.message-item.unread .message-icon {
  background: linear-gradient(135deg, var(--accent), var(--accent-light));
  color: white;
}

.message-item:not(.unread) .message-icon {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.message-text {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
}

.message-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.message-status {
  flex-shrink: 0;
}

.status-unread {
  padding: 4px 10px;
  background: transparent;
  border: 1px solid var(--accent);
  border-radius: 12px;
  color: var(--accent);
  font-size: 12px;
  font-weight: 500;
}

.status-read {
  padding: 4px 10px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  color: var(--text-tertiary);
  font-size: 12px;
}

.empty-state,
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.empty-state i,
.loading-state i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.4;
}

.empty-state p,
.loading-state p {
  font-size: 16px;
  margin: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-subtle);
}
</style>
