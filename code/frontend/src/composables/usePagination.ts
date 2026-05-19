import { ref, computed } from 'vue'

export interface PaginationOptions {
  initialPage?: number
  initialPageSize?: number
}

export interface UsePaginationReturn {
  page: typeof page.value
  pageSize: typeof pageSize.value
  total: typeof total.value
  pagination: typeof pagination.value
  onPageChange: (p: number) => void
  onPageSizeChange: (size: number) => void
  resetPagination: () => void
}

export function usePagination(options: PaginationOptions = {}) {
  const page = ref(options.initialPage ?? 1)
  const pageSize = ref(options.initialPageSize ?? 10)
  const total = ref(0)

  const pagination = computed(() => ({
    page: page.value,
    size: pageSize.value,
    total: total.value,
  }))

  function onPageChange(p: number) {
    page.value = p
  }

  function onPageSizeChange(size: number) {
    pageSize.value = size
    page.value = 1 // 改变pageSize时重置到第一页
  }

  function resetPagination() {
    page.value = 1
    total.value = 0
  }

  return {
    page,
    pageSize,
    total,
    pagination,
    onPageChange,
    onPageSizeChange,
    resetPagination,
  }
}