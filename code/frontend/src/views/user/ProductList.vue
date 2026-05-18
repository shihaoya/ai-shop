<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { getProducts, createOrder, getAddresses, getPoints } from '@/api/user'
import { message } from 'ant-design-vue'
import type { Product, Address } from '@/types/api'
import pcaData from 'china-division/dist/pca.json'
import CyberPagination from '@/components/CyberPagination.vue'

const themeStore = useThemeStore()
const userStore = useUserStore()

onMounted(() => {
  themeStore.init()
  loadProducts()
  loadPoints()
  loadAddresses()
})

const loading = ref(false)
const products = ref<Product[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })
const detailVisible = ref(false)
const selectedProduct = ref<Product | null>(null)

// 用户积分
const userPoints = ref(0)

// 兑换弹窗
const redeemVisible = ref(false)
const redeemQuantity = ref(1)
const redeemAddress = ref({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
})
const addressList = ref<Address[]>([])
const redeemLoading = ref(false)

// 地址选择弹窗
const addressPickerVisible = ref(false)

function openAddressPicker() {
  addressPickerVisible.value = true
}

function selectAddress(addr: Address) {
  redeemAddress.value = {
    receiver: addr.receiver,
    phone: addr.phone,
    province: addr.province,
    city: addr.city,
    district: addr.district,
    detail: addr.detail,
  }
  selectedRegion.value = [addr.province, addr.city, addr.district]
  addressPickerVisible.value = false
}

// 省市区级联数据
interface AreaNode {
  label: string
  value: string
  children?: AreaNode[]
}

function buildTree(data: Record<string, Record<string, string[]>>): AreaNode[] {
  return Object.entries(data).map(([province, citiesObj]) => ({
    label: province,
    value: province,
    children: Object.entries(citiesObj).map(([city, districts]) => ({
      label: city,
      value: city,
      children: districts.map(district => ({
        label: district,
        value: district,
      })),
    })),
  }))
}

const regionOptions = computed(() => buildTree(pcaData))

// 级联选择器选中的值 [省, 市, 区]
const selectedRegion = ref<string[]>([])

// 计算兑换后剩余积分
const remainingPoints = computed(() => {
  if (!selectedProduct.value) return 0
  const price = Number(selectedProduct.value.price) || 0
  const quantity = Number(redeemQuantity.value) || 0
  const points = Number(userPoints.value) || 0
  return points - price * quantity
})

// 判断是否为实物商品
const isPhysicalProduct = computed(() => {
  return selectedProduct.value && Number(selectedProduct.value.type) === 2
})

// 默认地址
const defaultAddress = computed(() => {
  return addressList.value.find(a => a.isDefault === 1)
})

const viewMode = ref<'table' | 'card'>('card')
const keyword = ref('')
let searchTimer: ReturnType<typeof setTimeout> | null = null

function onKeywordInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pagination.value.page = 1
    loadProducts()
  }, 400)
}

async function loadProducts() {
  loading.value = true
  try {
    const res = await getProducts({
      page: pagination.value.page,
      size: pagination.value.size,
      keyword: keyword.value || undefined,
    })
    products.value = res.list.map(p => ({
      ...p,
      id: String(p.id)
    }))
    pagination.value.total = res.total
  } finally {
    loading.value = false
  }
}

async function loadPoints() {
  try {
    const res = await getPoints()
    userPoints.value = res.points
  } catch (e: any) {
    console.error('获取积分失败', e)
  }
}

async function loadAddresses() {
  try {
    const res = await getAddresses()
    addressList.value = res.map(a => ({ ...a, id: String(a.id) }))
  } catch (e: any) {
    console.error('获取地址失败', e)
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function showDetail(product: Product) {
  selectedProduct.value = product
  detailVisible.value = true
}

function showRedeem(product: Product) {
  selectedProduct.value = product
  redeemQuantity.value = 1
  // 实物商品默认带入默认地址信息
  if (isPhysicalProduct.value && defaultAddress.value) {
    redeemAddress.value = {
      receiver: defaultAddress.value.receiver,
      phone: defaultAddress.value.phone,
      province: defaultAddress.value.province,
      city: defaultAddress.value.city,
      district: defaultAddress.value.district,
      detail: defaultAddress.value.detail,
    }
    selectedRegion.value = [
      defaultAddress.value.province,
      defaultAddress.value.city,
      defaultAddress.value.district,
    ]
  } else {
    redeemAddress.value = {
      receiver: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      detail: '',
    }
    selectedRegion.value = []
  }
  redeemVisible.value = true
}

// 级联选择器变化时更新 redeemAddress
function handleRegionChange(value: string[]) {
  selectedRegion.value = value
  if (value.length >= 3) {
    redeemAddress.value.province = value[0]
    redeemAddress.value.city = value[1]
    redeemAddress.value.district = value[2]
  } else {
    redeemAddress.value.province = value[0] || ''
    redeemAddress.value.city = value[1] || ''
    redeemAddress.value.district = value[2] || ''
  }
}

async function handleRedeem() {
  if (!selectedProduct.value) return

  // 积分不足
  if (remainingPoints.value < 0) {
    message.error('积分不足')
    return
  }

  // 实物商品必须填写完整地址
  if (isPhysicalProduct.value) {
    if (!redeemAddress.value.receiver || !redeemAddress.value.phone ||
        !redeemAddress.value.province || !redeemAddress.value.city ||
        !redeemAddress.value.district || !redeemAddress.value.detail) {
      message.error('请填写完整的收货地址')
      return
    }
  }

  redeemLoading.value = true
  try {
    await createOrder(
      selectedProduct.value.id,
      redeemQuantity.value,
      isPhysicalProduct.value ? redeemAddress.value : undefined
    )
    message.success('兑换成功！')
    redeemVisible.value = false
  } finally {
    redeemLoading.value = false
  }
  // 刷新积分、地址和商品列表
  loadPoints()
  loadAddresses()
  loadProducts()
}

function getStatusTag(status: number) {
  const map: Record<number, { text: string; class: string }> = {
    0: { text: '已下架', class: 'gray' },
    1: { text: '上架中', class: 'green' },
  }
  return map[status] ?? { text: '未知', class: 'gray' }
}

function getTypeText(type: string | number) {
  const t = Number(type)
  if (t === 1) return '虚拟'
  if (t === 2) return '实物'
  return String(type)
}
</script>

<template>
  <div id="page-product-list">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <!-- 页头：标题 + 视图切换 + 搜索 -->
      <div class="page-head">
        <h2><span class="accent-line"></span>商品列表</h2>
        <div class="page-toolbar">
          <div class="search-wrap">
            <i class="fas fa-search search-icon"></i>
            <input
              v-model="keyword"
              class="search-input"
              placeholder="搜索商品名称..."
              @input="onKeywordInput"
            />
          </div>
          <button class="view-btn" :class="{ active: viewMode === 'card' }" @click="viewMode = 'card'" title="卡片视图">
            <i class="fas fa-th-large"></i>
          </button>
          <button class="view-btn" :class="{ active: viewMode === 'table' }" @click="viewMode = 'table'" title="表格视图">
            <i class="fas fa-table"></i>
          </button>
        </div>
      </div>

      <!-- 表格视图 -->
      <div v-show="viewMode === 'table'" class="table-card">
        <a-spin :spinning="loading">
          <table v-if="products.length > 0" class="data-table">
            <thead>
              <tr>
                <th style="width:56px;">图片</th>
                <th>商品名称</th>
                <th>分类</th>
                <th>类型</th>
                <th>价格</th>
                <th>库存</th>
                <th style="width:90px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in products" :key="product.id">
                <td>
                  <img
                    v-if="(product as any).mainImageUrl"
                    :src="(product as any).mainImageUrl"
                    class="table-thumb"
                  />
                  <div v-else class="table-thumb-placeholder">
                    <i class="fas fa-box"></i>
                  </div>
                </td>
                <td><strong>{{ product.name }}</strong></td>
                <td>{{ product.categoryName || '-' }}</td>
                <td>
                  <span class="type-tag" :class="getTypeText(product.type) === '虚拟' ? 'blue' : 'orange'">
                    {{ getTypeText(product.type) }}
                  </span>
                </td>
                <td class="cell-price">{{ product.price }}</td>
                <td>{{ product.stock }}</td>
                <td>
                  <button class="redeem-btn-sm" @click="showRedeem(product)">兑换</button>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-else-if="!loading" class="empty-state">
            <i class="fas fa-store"></i>
            <p>暂无商品</p>
          </div>
        </a-spin>
      </div>

      <!-- 卡片视图 -->
      <div v-show="viewMode === 'card'" class="product-grid-wrap">
        <a-spin :spinning="loading">
          <div v-if="products.length === 0 && !loading" class="empty-state">
            <i class="fas fa-store"></i>
            <p>暂无商品</p>
          </div>
          <div v-else class="product-grid">
            <div
              v-for="product in products"
              :key="product.id"
              class="product-card"
            >
              <div class="card-img-wrap" @click="showDetail(product)">
                <img
                  v-if="(product as any).mainImageUrl"
                  :src="(product as any).mainImageUrl"
                  class="card-img"
                />
                <div v-else class="card-img-placeholder">
                  <i class="fas fa-box"></i>
                </div>
                <span :class="['card-status-tag', getStatusTag(product.status).class]">
                  {{ getStatusTag(product.status).text }}
                </span>
              </div>
              <div class="card-body" @click="showDetail(product)">
                <div class="card-name">{{ product.name }}</div>
                <div class="card-meta">
                  <span class="type-tag" :class="getTypeText(product.type) === '虚拟' ? 'blue' : 'orange'">
                    {{ getTypeText(product.type) }}
                  </span>
                  <span>{{ product.categoryName || '未分类' }}</span>
                </div>
                <div class="card-info">
                  <span class="price-value">{{ product.price }} 积分</span>
                  <span>库存 {{ product.stock }}</span>
                </div>
              </div>
              <div class="card-footer">
                <button class="redeem-btn" @click="showRedeem(product)">
                  <i class="fas fa-exchange-alt"></i> 兑换
                </button>
              </div>
            </div>
          </div>
        </a-spin>
      </div>

      <!-- Pagination -->
      <div v-if="pagination.total > 0" class="pagination-wrapper">
        <CyberPagination
          v-model:current="pagination.page"
          v-model:pageSize="pagination.size"
          :total="pagination.total"
          @change="handlePageChange"
        />
      </div>
    </div>

    <!-- 商品详情弹框 -->
    <a-modal
      v-model:open="detailVisible"
      :footer="null"
      :centered="true"
      :width="500"
      class="cyber-modal"
    >
      <template v-if="selectedProduct">
        <div class="detail-body">
          <div class="detail-image">
            <img
              v-if="(selectedProduct as any).mainImageUrl"
              :src="(selectedProduct as any).mainImageUrl"
            />
            <div v-else class="detail-image-placeholder">
              <i class="fas fa-box-open"></i>
            </div>
          </div>
          <h2 class="detail-title">{{ selectedProduct.name }}</h2>
          <div class="detail-meta-row">
            <span :class="['status-tag', getStatusTag(selectedProduct.status).class]">
              {{ getStatusTag(selectedProduct.status).text }}
            </span>
            <span class="detail-label">{{ getTypeText(selectedProduct.type) }}</span>
            <span class="detail-label">{{ selectedProduct.categoryName || '未分类' }}</span>
          </div>
          <div class="detail-price-row">
            <span class="detail-price-value">{{ selectedProduct.price }}</span>
            <span class="detail-price-unit">积分</span>
          </div>
          <div class="detail-stock-row">
            <i class="fas fa-boxes-stacked"></i>
            <span>库存: {{ selectedProduct.stock }}</span>
          </div>
          <p v-if="selectedProduct.description" class="detail-desc">
            {{ selectedProduct.description }}
          </p>
          <div class="detail-actions">
            <button class="redeem-btn detail-redeem" @click="showRedeem(selectedProduct)">
              <i class="fas fa-exchange-alt"></i> 立即兑换
            </button>
          </div>
        </div>
      </template>
    </a-modal>

    <!-- 兑换确认弹窗 -->
    <a-modal
      v-model:open="redeemVisible"
      :centered="true"
      :width="420"
      :confirm-loading="redeemLoading"
      title="确认兑换"
      ok-text="确认兑换"
      cancel-text="取消"
      class="cyber-modal"
      @ok="handleRedeem"
    >
      <div v-if="selectedProduct" class="redeem-form">
        <div class="redeem-product-info">
          <img
            v-if="(selectedProduct as any).mainImageUrl"
            :src="(selectedProduct as any).mainImageUrl"
            class="redeem-product-img"
          />
          <div v-else class="redeem-product-img-placeholder">
            <i class="fas fa-box"></i>
          </div>
          <div class="redeem-product-text">
            <div class="redeem-product-name">{{ selectedProduct.name }}</div>
            <div class="redeem-product-price">{{ selectedProduct.price }} 积分/件</div>
          </div>
        </div>

        <!-- 积分信息 -->
        <div class="redeem-points-info">
          <div class="points-row">
            <span class="points-label">当前积分</span>
            <span class="points-value">{{ userPoints }}</span>
          </div>
          <div class="points-row">
            <span class="points-label">兑换数量</span>
            <a-input-number
              v-model:value="redeemQuantity"
              :min="1"
              :max="selectedProduct.stock"
              :precision="0"
              style="width: 100px;"
            />
          </div>
          <div class="points-row">
            <span class="points-label">扣除积分</span>
            <span class="points-value text-danger">-{{ selectedProduct.price * redeemQuantity }}</span>
          </div>
          <div class="points-row">
            <span class="points-label">剩余积分</span>
            <span class="points-value" :class="{ 'text-danger': remainingPoints < 0 }">
              {{ remainingPoints }}
            </span>
          </div>
        </div>

        <!-- 实物商品地址表单 -->
        <div v-if="isPhysicalProduct" class="redeem-address">
          <div class="form-row" style="margin-top: 16px;">
            <div class="form-item" style="flex: 0 0 auto;">
              <button type="button" class="cyber-btn" @click="openAddressPicker" style="margin-top: 20px;">
                <i class="fas fa-book" style="margin-right:4px;"></i>
                选择地址
              </button>
            </div>
          </div>
          <div class="form-row" style="margin-top: 16px;">
            <div class="form-item">
              <label>收货人</label>
              <input v-model="redeemAddress.receiver" type="text" placeholder="请输入收货人" />
            </div>
            <div class="form-item">
              <label>联系电话</label>
              <input v-model="redeemAddress.phone" type="tel" placeholder="请输入联系电话" maxlength="11" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-item full">
              <label>省市区</label>
              <a-cascader
                v-model:value="selectedRegion"
                :options="regionOptions"
                placeholder="请选择省市区"
                change-on-select
                @change="handleRegionChange"
                style="width: 100%;"
              />
            </div>
          </div>
          <div class="form-item full">
            <label>详细地址</label>
            <input v-model="redeemAddress.detail" type="text" placeholder="请输入详细地址" />
          </div>
        </div>
      </div>

      <!-- 地址选择弹窗 -->
      <a-modal
        v-model:open="addressPickerVisible"
        title="选择收货地址"
        :centered="true"
        :width="500"
        :footer="null"
        class="cyber-modal"
      >
        <div class="address-picker-list">
          <div v-if="addressList.length === 0" class="empty-state" style="padding: 40px 20px; text-align: center;">
            <i class="fas fa-map-marker-alt" style="font-size: 36px; margin-bottom: 12px; opacity: 0.4;"></i>
            <p>暂无收货地址</p>
          </div>
          <div
            v-for="addr in addressList"
            :key="String(addr.id)"
            class="address-picker-item"
            :class="{ selected: redeemAddress.receiver === addr.receiver && redeemAddress.phone === addr.phone }"
            @click="selectAddress(addr)"
          >
            <div class="address-picker-main">
              <span class="receiver">{{ addr.receiver }}</span>
              <span class="phone">{{ addr.phone }}</span>
              <span v-if="addr.isDefault === 1" class="default-badge">默认</span>
            </div>
            <div class="address-picker-detail">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</div>
          </div>
        </div>
      </a-modal>
    </a-modal>
  </div>
</template>

<style scoped>
#page-product-list {
  min-height: 100vh;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
  padding: 20px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}
.page-head h2 {
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
}
.accent-line {
  display: inline-block;
  width: 4px;
  height: 20px;
  background: var(--accent);
  border-radius: 2px;
}
.page-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}
.search-wrap {
  position: relative;
  display: flex;
  align-items: center;
}
.search-icon {
  position: absolute;
  left: 10px;
  font-size: 13px;
  color: var(--text-muted);
  pointer-events: none;
}
.search-input {
  width: 200px;
  padding: 7px 10px 7px 30px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-input);
  color: var(--text-primary);
  border-radius: var(--radius);
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}
.search-input:focus {
  border-color: var(--accent);
}
.search-input::placeholder {
  color: var(--text-muted);
}
.view-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-subtle);
  background: var(--bg-card);
  color: var(--text-secondary);
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 15px;
  transition: all 0.2s;
}
.view-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}
.view-btn.active {
  background: rgba(var(--accent-rgb), 0.12);
  border-color: var(--accent);
  color: var(--accent);
}

/* 卡片网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  position: relative;
  min-height: 300px;
}

.product-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s ease;
}

.product-card:hover {
  border-color: var(--border-glow);
  box-shadow: 0 4px 20px rgba(var(--accent-rgb), 0.10);
  transform: translateY(-2px);
}

.card-img-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: var(--bg-input);
  overflow: hidden;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.card-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 36px;
  opacity: 0.4;
}

.card-status-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  backdrop-filter: blur(4px);
}
.card-status-tag.green { background: rgba(16,185,129,0.75); color: #fff; }
.card-status-tag.gray { background: rgba(156,163,175,0.6);  color: #fff; }

.card-body {
  padding: 10px 12px 12px;
}

.card-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 6px;
}

.type-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
}
.type-tag.blue { background: rgba(6,182,212,0.1); color: var(--cyan); }
.type-tag.orange { background: rgba(245,158,11,0.1); color: var(--orange); }

.card-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
}
.card-info .price-value {
  font-weight: 700;
  color: var(--accent-light);
}

.card-footer {
  padding: 8px 12px 10px;
  border-top: 1px solid var(--border-subtle);
}

/* 兑换按钮 */
.redeem-btn {
  width: 100%;
  padding: 7px 0;
  border: 1px solid var(--accent);
  background: linear-gradient(135deg, rgba(var(--accent-rgb),0.12), rgba(var(--accent-rgb),0.06));
  color: var(--accent);
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}
.redeem-btn:hover {
  background: var(--accent);
  color: #fff;
}
.redeem-btn-sm {
  padding: 4px 12px;
  border: 1px solid var(--accent);
  background: transparent;
  color: var(--accent);
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s;
}
.redeem-btn-sm:hover {
  background: var(--accent);
  color: #fff;
}

/* 表格视图 */
.table-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  overflow: hidden;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.data-table th {
  padding: 12px 10px;
  text-align: left;
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 12px;
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(var(--accent-rgb),0.03);
}
.data-table td {
  padding: 10px;
  border-bottom: 1px solid var(--border-subtle);
  color: var(--text-primary);
}
.data-table tr:last-child td { border-bottom: none; }
.data-table tr:hover td { background: rgba(var(--accent-rgb),0.02); }
.table-thumb {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
  display: block;
}
.table-thumb-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  background: var(--bg-input);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 16px;
}
.cell-price {
  font-weight: 700;
  color: var(--accent-light);
}

.product-grid-wrap {
  min-height: 300px;
}

/* 详情弹框兑换按钮 */
.detail-actions {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--border-subtle);
}
.detail-redeem {
  width: 100%;
  padding: 10px 0;
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-secondary);
}
.empty-state i {
  font-size: 56px;
  margin-bottom: 16px;
  opacity: 0.4;
}
.empty-state p { font-size: 16px; }

/* 分页 */
.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 20px 0 8px;
  margin-top: 12px;
  border-top: 1px solid var(--border-subtle);
}

/* 弹框 */
.cyber-modal :deep(.ant-modal-content) {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  box-shadow: 0 0 40px rgba(0,0,0,0.4);
}
.cyber-modal :deep(.ant-modal-close) { color: var(--text-secondary); }
.cyber-modal :deep(.ant-modal-close:hover) { color: var(--text-primary); }

.detail-body {
  padding: 4px;
}
.detail-image {
  width: 100%;
  height: 240px;
  border-radius: var(--radius);
  overflow: hidden;
  background: var(--bg-input);
  margin-bottom: 16px;
}
.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.detail-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 64px;
}
.detail-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
}
.detail-meta-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}
.status-tag.green { background: rgba(16,185,129,0.15); color: #10b981; }
.status-tag.gray { background: rgba(156,163,175,0.15); color: #9ca3af; }
.detail-label {
  font-size: 12px;
  color: var(--text-secondary);
}
.detail-price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 10px;
}
.detail-price-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--accent-light);
  text-shadow: var(--accent-glow-text);
}
.detail-price-unit { font-size: 12px; color: var(--text-muted); }
.detail-stock-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 14px;
}
.detail-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding-top: 12px;
  border-top: 1px solid var(--border-subtle);
}

/* 兑换弹窗样式 */
.redeem-form {
  margin-top: 16px;
}

.redeem-form .form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.redeem-form .form-item {
  flex: 1;
}

.redeem-form .form-item.full {
  flex: none;
  width: 100%;
}

.redeem-form label {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.redeem-form input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
  box-sizing: border-box;
}

.redeem-form input:focus {
  outline: none;
  border-color: var(--accent);
}

/* 地址选择弹窗样式 */
.address-picker-list {
  max-height: 400px;
  overflow-y: auto;
}

.address-picker-item {
  padding: 14px 16px;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.address-picker-item:hover {
  border-color: var(--accent);
  background: rgba(var(--accent-rgb), 0.04);
}

.address-picker-item.selected {
  border-color: var(--accent);
  background: rgba(var(--accent-rgb), 0.08);
}

.address-picker-main {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.address-picker-main .receiver {
  font-weight: 600;
  color: var(--text-primary);
}

.address-picker-main .phone {
  color: var(--text-secondary);
  font-size: 13px;
}

.address-picker-main .default-badge {
  background: var(--accent);
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.address-picker-detail {
  font-size: 13px;
  color: var(--text-secondary);
}

.redeem-product-info {
  background: var(--bg-input);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.redeem-product-img {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid var(--border-subtle);
}

.redeem-product-img-placeholder {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  border: 1px solid var(--border-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 20px;
}

.redeem-product-text {
  flex: 1;
  min-width: 0;
}

.redeem-product-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.redeem-product-price {
  font-size: 13px;
  color: var(--accent);
  font-weight: 600;
}

.redeem-product-img {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.redeem-product-img-placeholder {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.redeem-product-text {
  flex: 1;
  min-width: 0;
}

.redeem-product-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.redeem-product-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.redeem-product-price {
  font-size: 13px;
  color: var(--accent);
  font-weight: 600;
}

.redeem-points-info {
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.points-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.points-row:not(:last-child) {
  border-bottom: 1px solid var(--border-subtle);
}

.points-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.points-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.points-value.text-danger {
  color: #ef4444;
}

/* 地址表单样式（与 AddressList 一致） */
.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.form-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.form-item input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
  transition: all 0.2s;
}

.form-item input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-glow);
}
</style>
