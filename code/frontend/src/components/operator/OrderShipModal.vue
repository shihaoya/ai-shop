<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { Order } from '@/types/api'

interface Props {
  visible: boolean
  order: Order | null
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'ship', orderId: string, trackingNo: string, carrier: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const shipForm = ref({ expressCompany: '', expressNo: '' })
const shipLoading = ref(false)

watch(() => props.visible, (val) => {
  if (val) {
    shipForm.value = { expressCompany: '', expressNo: '' }
  }
})

function close() {
  emit('update:visible', false)
}

async function handleShip() {
  if (!shipForm.value.expressCompany.trim()) {
    message.warning('请输入物流公司')
    return
  }
  if (!shipForm.value.expressNo.trim()) {
    message.warning('请输入物流单号')
    return
  }
  if (!props.order) return

  shipLoading.value = true
  try {
    emit('ship', props.order.id, shipForm.value.expressNo.trim(), shipForm.value.expressCompany.trim())
  } finally {
    shipLoading.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible && order" class="modal-overlay" @click.self="close">
      <div class="modal-card">
        <div class="modal-header">
          <h3><span class="accent-line"></span>发货</h3>
          <button class="modal-close" @click="close">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-item">
            <label>物流公司</label>
            <input
              v-model="shipForm.expressCompany"
              type="text"
              placeholder="请输入物流公司名称"
              class="cyber-input"
            />
          </div>
          <div class="form-item">
            <label>物流单号</label>
            <input
              v-model="shipForm.expressNo"
              type="text"
              placeholder="请输入物流单号"
              class="cyber-input"
            />
          </div>
          <div class="modal-actions">
            <button class="cyber-btn" @click="close">取消</button>
            <button class="cyber-btn-primary" :disabled="shipLoading" @click="handleShip">
              <i v-if="shipLoading" class="fas fa-spinner fa-spin"></i>
              <span v-else>确认发货</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(7, 8, 22, 0.8);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-card {
  background: var(--bg-card);
  border: 1px solid var(--border-glow);
  border-radius: var(--radius);
  width: 480px;
  max-width: 90vw;
  display: flex;
  flex-direction: column;
  box-shadow: var(--accent-glow), 0 20px 50px rgba(0,0,0,0.4);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  transition: color 0.3s;
}

.modal-close:hover {
  color: var(--accent);
}

.modal-body {
  padding: 20px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid var(--border-subtle);
  margin-top: 16px;
}

/* 表单样式 */
.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 6px;
}

.cyber-input {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-input);
  color: var(--text-primary);
  border-radius: var(--radius);
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.cyber-input:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.10);
}

.cyber-btn {
  padding: 9px 20px;
  border: 1px solid var(--border-subtle);
  background: transparent;
  color: var(--text-secondary);
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.cyber-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.cyber-btn-primary {
  padding: 9px 20px;
  border: 1px solid var(--accent);
  background: var(--accent);
  color: #fff;
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.cyber-btn-primary:hover {
  opacity: 0.9;
}

.cyber-btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>