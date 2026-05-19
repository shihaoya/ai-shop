import { ref, computed } from 'vue'
import { getMyShop, applyShop, changeShopStatus } from '@/api/modules/operator'
import { message, Modal } from 'ant-design-vue'
import type { Shop } from '@/types/api'
import { ShopStatus } from '@/types/enums'
import { useOperatorShop } from './useOperatorShop'

export type ShopStatusType = 'none' | 'pending' | 'rejected' | 'approved'

export interface UseShopReturn {
  shop: typeof shop.value
  loading: typeof loading.value
  shopStatus: typeof shopStatus.value
  loadMyShop: () => Promise<void>
  handleApply: (name: string, description: string) => Promise<void>
  handleToggleStatus: () => void
  applyModalVisible: typeof applyModalVisible.value
  applyForm: typeof applyForm.value
  applyLoading: typeof applyLoading.value
  openApplyModal: () => void
}

export function useShop() {
  const { setHasShop } = useOperatorShop()

  const loading = ref(false)
  const shop = ref<Shop | null>(null)
  const applyModalVisible = ref(false)
  const applyForm = ref({ name: '', description: '' })
  const applyLoading = ref(false)

  // 判断店铺状态
  const shopStatus = computed<ShopStatusType>(() => {
    if (!shop.value) return 'none' // 无店铺
    if (shop.value.status === ShopStatus.PENDING) return 'pending' // 待审核
    if (shop.value.status === ShopStatus.REJECTED) return 'rejected' // 被拒绝
    return 'approved' // 已通过
  })

  async function loadMyShop() {
    loading.value = true
    const res: any = await getMyShop()
    if (res && res.hasShop === false) {
      shop.value = null
      setHasShop(false, null)
    } else if (res) {
      shop.value = {
        id: String(res.id),
        name: res.name,
        description: res.description,
        status: res.status,
        isActive: res.isActive,
        createdAt: res.createdAt,
      }
      setHasShop(true, res.status ?? null)
    } else {
      shop.value = null
    }
    loading.value = false
  }

  function openApplyModal() {
    applyForm.value = { name: '', description: '' }
    applyModalVisible.value = true
  }

  async function handleApply(name: string, description: string) {
    if (!name.trim()) {
      message.warning('请输入店铺名称')
      return
    }
    applyLoading.value = true
    try {
      await applyShop(name.trim(), description.trim())
      message.success('申请已提交，请等待审核')
      applyModalVisible.value = false
      loadMyShop()
    } finally {
      applyLoading.value = false
    }
  }

  function handleToggleStatus() {
    if (!shop.value) return
    const newStatus = shop.value.isActive === 1 ? 0 : 1
    const actionText = newStatus === 1 ? '营业' : '歇业'

    Modal.confirm({
      title: '切换营业状态',
      content: `确定要将店铺设置为"${actionText}"吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        await changeShopStatus(newStatus)
        message.success(`店铺已设置为${actionText}`)
        loadMyShop()
      }
    })
  }

  return {
    shop,
    loading,
    shopStatus,
    loadMyShop,
    handleApply,
    handleToggleStatus,
    applyModalVisible,
    applyForm,
    applyLoading,
    openApplyModal,
  }
}