<script setup lang="ts">
import { computed } from 'vue'
import type { Order } from '@/types/api'
import { OrderStatusText, OrderStatusClass } from '@/types/enums'

interface Props {
  visible: boolean
  order: Order | null
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', order: Order): void
  (e: 'close', order: Order): void
  (e: 'ship', order: Order): void
  (e: 'complete', order: Order): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

function getStatusTag(status: number) {
  return {
    text: OrderStatusText[status] || '未知',
    class: OrderStatusClass[status] || 'gray',
  }
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

function close() {
  emit('update:visible', false)
}

function onConfirm(order: Order) {
  emit('confirm', order)
}

function onClose(order: Order) {
  emit('close', order)
}

function onShip(order: Order) {
  emit('ship', order)
}

function onComplete(order: Order) {
  emit('complete', order)
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible && order" class="modal-overlay" @click.self="close">
      <div class="modal-card">
        <div class="modal-header">
          <h3><span class="accent-line"></span>订单详情</h3>
          <button class="modal-close" @click="close">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="detail-grid">
            <div class="detail-item">
              <label>订单号</label>
              <span class="id-cell">{{ order.orderNo }}</span>
            </div>
            <div class="detail-item">
              <label>订单状态</label>
              <span class="status-tag" :class="getStatusTag(order.status).class">
                <span class="dot"></span>{{ getStatusTag(order.status).text }}
              </span>
            </div>
            <div class="detail-item">
              <label>用户</label>
              <span>{{ order.userNickname || order.userId }}</span>
            </div>
            <div class="detail-item">
              <label>商品</label>
              <span>{{ order.productName }}</span>
            </div>
            <div class="detail-item">
              <label>数量</label>
              <span>x{{ order.quantity }}</span>
            </div>
            <div class="detail-item">
              <label>总积分</label>
              <span class="points-value">{{ order.totalPoints }}</span>
            </div>
            <div class="detail-item" v-if="order.expressNo">
              <label>物流单号</label>
              <span>{{ order.expressNo }}</span>
            </div>
            <div class="detail-item" v-if="order.expressCompany">
              <label>物流公司</label>
              <span>{{ order.expressCompany }}</span>
            </div>
            <div class="detail-item" v-if="order.closeReason">
              <label>关闭原因</label>
              <span class="text-red">{{ order.closeReason }}</span>
            </div>
            <!-- 收货地址信息 -->
            <div class="detail-item full-width" v-if="order.receiverName">
              <label>收货地址</label>
              <div class="address-info">
                <div class="address-row">
                  <span class="address-label">收货人</span>
                  <span class="address-value">{{ order.receiverName }}</span>
                </div>
                <div class="address-row">
                  <span class="address-label">手机号</span>
                  <span class="address-value">{{ order.receiverPhone }}</span>
                </div>
                <div class="address-row">
                  <span class="address-label">地址</span>
                  <span class="address-value">
                    {{ order.receiverProvince }}{{ order.receiverCity }}{{ order.receiverDistrict }}
                  </span>
                </div>
                <div class="address-detail" v-if="order.receiverDetail">
                  {{ order.receiverDetail }}
                </div>
              </div>
            </div>
            <div class="detail-item full-width">
              <label>下单时间</label>
              <span>{{ formatDate(order.createdAt) }}</span>
            </div>
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
  width: 520px;
  max-width: 90vw;
  max-height: 85vh;
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
  overflow-y: auto;
  flex: 1;
}

/* 详情网格 */
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-item label {
  font-size: 12px;
  color: var(--text-muted);
}

.detail-item .id-cell {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--accent);
}

.points-value {
  color: var(--accent-light);
  font-weight: 600;
  font-size: 15px;
}

.text-red {
  color: #ef4444;
}

/* 地址信息样式 */
.address-info {
  background: var(--bg-input);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  padding: 14px 16px;
  margin-top: 8px;
}

.address-info .address-row {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
}

.address-info .address-row:last-child {
  margin-bottom: 0;
}

.address-info .address-label {
  color: var(--text-muted);
  min-width: 56px;
}

.address-info .address-value {
  color: var(--text-primary);
}

.address-info .address-detail {
  color: var(--text-secondary);
  font-size: 12px;
  margin-top: 4px;
  padding-left: 64px;
}
</style>