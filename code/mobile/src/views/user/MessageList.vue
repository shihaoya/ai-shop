<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { getMessages, markMessageRead } from '@/api/user'
import type { Message } from '@/types'

const messages = ref<Message[]>([])
const loading = ref(false)

async function fetchMessages() {
  loading.value = true
  try {
    const res = await getMessages({ page: 1, size: 100 })
    messages.value = res.list
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

async function handleCardClick(m: Message) {
  if (m.isRead) return
  try {
    await markMessageRead(m.id)
    m.isRead = 1
  } catch {
    showToast('操作失败')
  }
}

onMounted(() => {
  fetchMessages()
})
</script>

<template>
  <div class="message-list-page">
    <van-nav-bar title="我的消息" />
    <div class="content">
      <div v-for="m in messages" :key="m.id" class="message-card" @click="handleCardClick(m)">
        <div class="message-top">
          <div class="message-title">{{ m.title }}</div>
          <span class="status-tag" :class="m.isRead ? 'read' : 'unread'">
            {{ m.isRead ? '已读' : '未读' }}
          </span>
        </div>
        <div class="message-body">{{ m.content }}</div>
        <div class="message-footer">
          <span class="message-time">{{ m.createdAt?.slice(0, 16).replace('T', ' ') }}</span>
        </div>
      </div>
      <van-empty v-if="!loading && messages.length === 0" description="暂无消息" />
    </div>
  </div>
</template>

<style scoped>
.message-list-page {
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
}

.content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.message-card {
  margin: 10px 12px;
  background: var(--bg-card);
  border-radius: 12px;
  overflow: hidden;
}

.message-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 14px 10px;
}

.message-title {
  font-size: 15px;
  font-weight: 600;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
  margin-left: 8px;
}

.status-tag.unread { background: #fee2e2; color: #ef4444; }
.status-tag.read { background: #f5f5f5; color: #8b8ba7; }

.message-body {
  font-size: 14px;
  color: var(--text-primary);
  padding: 0 14px 14px;
  line-height: 1.6;
}

.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 14px 12px;
}

.message-time {
  font-size: 12px;
  color: var(--text-muted);
}
</style>