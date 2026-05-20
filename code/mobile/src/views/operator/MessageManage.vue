<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { getMessages, createMessage, deleteMessage } from '@/api/message'
import type { Message } from '@/types'

const messages = ref<Message[]>([])
const loading = ref(false)
const showForm = ref(false)
const form = ref({ title: '', content: '', userId: '' })

async function fetchMessages() {
  loading.value = true
  try {
    messages.value = await getMessages({ page: 1, size: 100 })
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

async function handleSend() {
  if (!form.value.title || !form.value.content) {
    showToast('请填写完整')
    return
  }
  try {
    await createMessage(form.value)
    showToast('发送成功')
    showForm.value = false
    form.value = { title: '', content: '', userId: '' }
    fetchMessages()
  } catch {
    showToast('发送失败')
  }
}

async function handleDelete(id: string) {
  try {
    await deleteMessage(id)
    showToast('删除成功')
    fetchMessages()
  } catch {
    showToast('删除失败')
  }
}

onMounted(() => {
  fetchMessages()
})
</script>

<template>
  <div class="message-manage-page">
    <van-nav-bar title="消息管理">
      <template #right>
        <div class="nav-add" @click="showForm = true">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        </div>
      </template>
    </van-nav-bar>
    <div class="content">
      <div v-for="m in messages" :key="m.id" class="message-card">
        <div class="message-top">
          <div class="message-title">{{ m.title }}</div>
          <span class="status-tag" :class="m.isRead ? 'read' : 'unread'">
            {{ m.isRead ? '已读' : '未读' }}
          </span>
        </div>
        <div class="message-body">{{ m.content }}</div>
        <div class="message-time">{{ m.createdAt?.slice(0, 16).replace('T', ' ') }}</div>
        <div class="message-actions">
          <van-button size="small" class="action-btn btn-danger" @click="handleDelete(m.id)">删除</van-button>
        </div>
      </div>
      <van-empty v-if="!loading && messages.length === 0" description="暂无消息" />
    </div>

    <van-popup v-model:show="showForm" position="bottom" round>
      <div class="form-panel">
        <div class="panel-title">发送消息</div>
        <van-field v-model="form.title" label="标题" placeholder="请输入标题" />
        <van-field v-model="form.content" type="textarea" label="内容" placeholder="请输入内容" rows="3" />
        <van-button type="primary" block class="submit-btn" @click="handleSend">发送</van-button>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.message-manage-page {
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
}

.nav-add {
  display: flex;
  align-items: center;
  justify-content: center;
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
  font-size: 13px;
  color: var(--text-secondary);
  padding: 0 14px 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.message-time {
  font-size: 12px;
  color: var(--text-muted);
  padding: 0 14px 12px;
}

.message-actions {
  display: flex;
  border-top: 1px solid var(--border-subtle);
}

.action-btn {
  flex: 1;
  border: none;
  border-radius: 0;
  font-size: 12px;
  height: 36px;
}

.btn-danger { color: #ef4444; background: #fff5f5; }

.form-panel {
  padding: 20px 16px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
}

.submit-btn {
  margin-top: 16px;
  border-radius: 8px;
}
</style>