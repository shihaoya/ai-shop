<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { getMessages, markMessageRead } from '@/api/user'
import type { Message } from '@/types'

const messages = ref<Message[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const size = ref(10)

async function fetchMessages() {
  loading.value = true
  try {
    const res = await getMessages({ page: page.value, size: size.value })
    if (page.value === 1) messages.value = res.list
    else messages.value.push(...res.list)
    if (res.list.length < size.value) finished.value = true
    page.value++
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function onLoad() {
  if (!loading.value && !finished.value) fetchMessages()
}

async function handleMarkRead(msg: Message) {
  if (msg.isRead) return
  try {
    await markMessageRead(msg.id)
    msg.isRead = 1
  } catch {
    // ignore
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
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div v-for="msg in messages" :key="msg.id" class="message-card" :class="{ unread: !msg.isRead }" @click="handleMarkRead(msg)">
          <div class="message-header">
            <div class="message-icon" :class="{ unread: !msg.isRead }">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            </div>
            <div class="message-title-wrap">
              <div class="message-title">{{ msg.title }}</div>
              <van-tag v-if="!msg.isRead" type="danger" size="small">未读</van-tag>
            </div>
          </div>
          <div class="message-body">{{ msg.content }}</div>
          <div class="message-time">{{ msg.createdAt?.slice(0, 16).replace('T', ' ') }}</div>
        </div>
        <van-empty v-if="!loading && messages.length === 0" description="暂无消息" />
      </van-list>
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
  padding: 14px;
  background: var(--bg-card);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.message-card.unread {
  border-left: 3px solid var(--accent);
}

.message-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.message-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
}

.message-icon.unread {
  background: rgba(99, 102, 241, 0.1);
  color: var(--accent);
}

.message-title-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-title {
  font-size: 15px;
  font-weight: 600;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-body {
  font-size: 14px;
  color: var(--text-secondary);
  background: var(--bg-primary);
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 10px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.message-time {
  font-size: 12px;
  color: var(--text-muted);
  text-align: right;
}
</style>