import { ref } from 'vue'
import { getOrders } from '@/api/modules/operator'
import type { Order } from '@/types/api'
import { OrderStatus } from '@/types/enums'
import { usePagination } from './usePagination'

export interface OrderSearchParams {
  keyword?: string
  status?: number
}

export interface UseOrderSearchReturn {
  orders: typeof orders.value
  loading: typeof loading.value
  searchParams: typeof searchParams.value
  statusOptions: typeof statusOptions.value
  pagination: typeof pagination.value
  page: typeof page.value
  pageSize: typeof pageSize.value
  total: typeof total.value
  loadOrders: () => Promise<void>
  handleSearch: () => void
  handleReset: () => void
  handleStatusChange: (status: number | undefined) => void
  onPageChange: (p: number) => void
  onPageSizeChange: (size: number) => void
  resetPagination: () => void
}

export function useOrderSearch() {
  const { page, pageSize, total, pagination, onPageChange, onPageSizeChange, resetPagination } = usePagination({
    initialPage: 1,
    initialPageSize: 10,
  })

  const loading = ref(false)
  const orders = ref<Order[]>([])
  const searchParams = ref<OrderSearchParams>({
    keyword: '',
    status: undefined,
  })

  const statusOptions = [
    { label: '全部状态', value: undefined },
    { label: '待确认', value: OrderStatus.PENDING },
    { label: '已确认', value: OrderStatus.CONFIRMED },
    { label: '已发货', value: OrderStatus.SHIPPED },
    { label: '已完成', value: OrderStatus.COMPLETED },
    { label: '已关闭', value: OrderStatus.CLOSED },
  ]

  async function loadOrders() {
    loading.value = true
    try {
      const params: any = { page: page.value, size: pageSize.value }
      if (searchParams.value.keyword) params.keyword = searchParams.value.keyword
      if (searchParams.value.status !== undefined) params.status = searchParams.value.status

      const res = await getOrders(params)
      orders.value = res.list.map((o: Order) => ({
        ...o,
        id: String(o.id)
      }))
      total.value = res.total
    } finally {
      loading.value = false
    }
  }

  function handleSearch() {
    resetPagination()
    loadOrders()
  }

  function handleReset() {
    searchParams.value = { keyword: '', status: undefined }
    resetPagination()
    loadOrders()
  }

  function handleStatusChange(status: number | undefined) {
    searchParams.value.status = status
    resetPagination()
    loadOrders()
  }

  return {
    orders,
    loading,
    searchParams,
    statusOptions,
    pagination,
    page,
    pageSize,
    total,
    loadOrders,
    handleSearch,
    handleReset,
    handleStatusChange,
    onPageChange,
    onPageSizeChange,
    resetPagination,
  }
}