<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { useThemeStore } from '@/stores/theme'
import { getPointsLog } from '@/api/operator'
import type { PointsLog } from '@/types/api'

const themeStore = useThemeStore()
const route = useRoute()

// 列表数据
const dataList = ref<PointsLog[]>([])
const loading = ref(false)

// 分页
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

// 用户ID
const userId = computed(() => {
  const id = route.params.id
  return Array.isArray(id) ? id[0] : id
})

// 获取列表数据
const fetchData = async () => {
  if (!userId.value) return

  loading.value = true
  try {
    const res = await getPointsLog(userId.value, {
      page: pagination.value.current,
      size: pagination.value.pageSize
    })
    if (res) {
      dataList.value = res.list.map((item: PointsLog) => ({
        ...item,
        id: String(item.id) // 雪花ID转String
      }))
      pagination.value.total = res.total
    } else {
      message.error('获取积分流水失败')
    }
  } catch (e) {
    throw e
  } finally {
    loading.value = false
  }
}

// 分页变化
const handlePageChange = (page: number, size: number) => {
  pagination.value.current = page
  pagination.value.pageSize = size
  fetchData()
}

// 格式化时间
const formatTime = (time: string | undefined) => {
  if (!time) return '-'
  return time
}

// 类型标签
const getTypeTag = (type: number) => {
  return type === 1
    ? { text: '增加', color: '#10b981' }
    : { text: '扣除', color: '#ef4444' }
}

// 列定义
const columns = [
  {
    title: '时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 180,
    customRender: ({ text }: { text: string }) => formatTime(text)
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
    width: 100,
    customRender: ({ text }: { text: number }) => {
      const tag = getTypeTag(text)
      return `<span style="color:${tag.color};font-weight:600;">${tag.text}</span>`
    }
  },
  {
    title: '积分变动',
    dataIndex: 'amount',
    key: 'amount',
    width: 120,
    customRender: ({ text, record }: { text: number, record: PointsLog }) => {
      const color = record.type === 1 ? '#10b981' : '#ef4444'
      const sign = record.type === 1 ? '+' : '-'
      return `<span style="color:${color};font-weight:700;">${sign}${text}</span>`
    }
  },
  {
    title: '余额',
    dataIndex: 'balance',
    key: 'balance',
    width: 120,
    customRender: ({ text }: { text: number }) => `<span style="font-weight:600;">${text}</span>`
  },
  {
    title: '备注',
    dataIndex: 'remark',
    key: 'remark',
    ellipsis: true,
    customRender: ({ text }: { text: string }) => text || '-'
  },
  {
    title: '操作人',
    dataIndex: 'operatorName',
    key: 'operatorName',
    width: 120,
    customRender: ({ text }: { text: string }) => text || '-'
  }
]

onMounted(() => {
  themeStore.init()
  fetchData()
})
</script>

<template>
  <div id="page-points-log">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>积分流水</h2>
      </div>

      <div class="cyber-card">
        <a-table
          :columns="columns"
          :data-source="dataList"
          :loading="loading"
          :pagination="false"
          :scroll="{ x: 800 }"
          row-key="id"
          :custom-row="() => ({ class: 'cyber-table-row' })"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'createdAt'">
              {{ formatTime(record.createdAt) }}
            </template>
            <template v-else-if="column.key === 'type'">
              <span :style="{ color: getTypeTag(record.type).color, fontWeight: 600 }">
                {{ getTypeTag(record.type).text }}
              </span>
            </template>
            <template v-else-if="column.key === 'amount'">
              <span :style="{ color: record.type === 1 ? '#10b981' : '#ef4444', fontWeight: 700 }">
                {{ record.type === 1 ? '+' : '-' }}{{ record.amount }}
              </span>
            </template>
            <template v-else-if="column.key === 'balance'">
              <span style="font-weight:600;">{{ record.balance }}</span>
            </template>
            <template v-else-if="column.key === 'remark'">
              {{ record.remark || '-' }}
            </template>
            <template v-else-if="column.key === 'operatorName'">
              {{ record.operatorName || '-' }}
            </template>
          </template>

          <template #emptyText>
            <div style="text-align:center;padding:40px 20px;color:var(--text-secondary);">
              <i class="fas fa-coins" style="font-size:48px;margin-bottom:16px;opacity:0.5;"></i>
              <p style="font-size:16px;">暂无积分流水记录</p>
            </div>
          </template>
        </a-table>

        <div class="pagination-wrapper" v-if="pagination.total > 0">
          <a-pagination
            v-model:current="pagination.current"
            v-model:pageSize="pagination.pageSize"
            :total="pagination.total"
            :show-size-changer="true"
            :show-total="(total: number) => `共 ${total} 条`"
            @change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0 8px;
}

:deep(.cyber-table-row:hover) {
  background: rgba(var(--accent-rgb), 0.05);
}
</style>