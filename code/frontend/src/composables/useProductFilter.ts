import { ref, computed } from 'vue'
import { getProducts, getCategories } from '@/api/modules/operator'
import type { Product, Category } from '@/types/api'
import { usePagination } from './usePagination'

export interface UseProductFilterReturn {
  products: typeof products.value
  categories: typeof categories.value
  loading: typeof loading.value
  categoryOptions: typeof categoryOptions.value
  pagination: typeof pagination.value
  page: typeof page.value
  pageSize: typeof pageSize.value
  total: typeof total.value
  loadProducts: () => Promise<void>
  loadCategories: () => Promise<void>
  ensureCategories: () => Promise<void>
  onPageChange: (p: number) => void
  onPageSizeChange: (size: number) => void
  resetPagination: () => void
}

export function useProductFilter() {
  const { page, pageSize, total, pagination, onPageChange, onPageSizeChange, resetPagination } = usePagination({
    initialPage: 1,
    initialPageSize: 10,
  })

  const loading = ref(false)
  const products = ref<Product[]>([])
  const categories = ref<Category[]>([])

  const categoryOptions = computed(() =>
    categories.value.map(c => ({ label: c.name, value: c.id }))
  )

  async function loadProducts() {
    loading.value = true
    try {
      const res = await getProducts({ page: page.value, size: pageSize.value })
      products.value = res.list.map((p: Product) => ({
        ...p,
        id: String(p.id)
      }))
      total.value = res.total
    } catch (e: any) {
      // 全局拦截器已处理错误提示，组件内不重复处理
    } finally {
      loading.value = false
    }
  }

  async function loadCategories() {
    try {
      const res = await getCategories()
      categories.value = res.map((c: Category) => ({
        ...c,
        id: String(c.id)
      }))
    } catch (e: any) {
      // 全局拦截器已处理错误提示，组件内不重复处理
    }
  }

  async function ensureCategories() {
    if (categories.value.length === 0) {
      await loadCategories()
    }
  }

  return {
    products,
    categories,
    loading,
    categoryOptions,
    pagination,
    page,
    pageSize,
    total,
    loadProducts,
    loadCategories,
    ensureCategories,
    onPageChange,
    onPageSizeChange,
    resetPagination,
  }
}