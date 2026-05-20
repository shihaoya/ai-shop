<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getProduct, updateProduct, getCategories, createProduct } from '@/api/product'
import ImageUploader from '@/components/ImageUploader.vue'
import SelectPicker from '@/components/SelectPicker.vue'
import type { Category } from '@/types'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const productId = computed(() => route.params.id as string)

const loading = ref(false)
const categories = ref<Category[]>([])

const form = ref({
  name: '',
  categoryId: '',
  type: '1' as string,
  price: 0,
  stock: 0,
  limitPerUser: 0,
  mainImage: '',
  detailImages: [] as string[],
  description: ''
})

const categoryOptions = computed(() =>
  categories.value.map(c => ({ text: c.name, value: c.id }))
)

const submitText = computed(() => isEdit.value ? '保存' : '创建')

onMounted(async () => {
  try {
    const cats = await getCategories()
    categories.value = cats

    if (isEdit.value) {
      const p = await getProduct(productId.value)
      form.value = {
        name: p.name,
        categoryId: p.categoryId,
        type: String(p.type),
        price: p.price,
        stock: p.stock,
        limitPerUser: (p as any).limitPerUser || 0,
        mainImage: (p as any).mainImage || '', // 存 ID，组件通过 getFilesByIds 转 URL 显示
        detailImages: ((p as any).detailImages || '').split(',').filter(Boolean), // 存 ID 数组
        description: p.description || ''
      }
    }
  } catch {
    showToast('加载失败')
    router.back()
  } finally {
    loading.value = false
  }
})

async function handleSubmit() {
  if (!form.value.name.trim()) {
    showToast('请输入商品名称')
    return
  }
  if (form.value.price <= 0) {
    showToast('请输入有效的积分价格')
    return
  }
  if (form.value.stock < 0) {
    showToast('请输入有效的库存')
    return
  }
  if (form.value.limitPerUser < 0) {
    showToast('单人限购不能为负数')
    return
  }

  try {
    const data = {
      name: form.value.name,
      categoryId: form.value.categoryId,
      type: Number(form.value.type),
      price: Number(form.value.price) || 0,
      stock: Number(form.value.stock) || 0,
      limitPerUser: Number(form.value.limitPerUser) || 0,
      mainImage: form.value.mainImage,
      detailImages: form.value.detailImages.join(','),
      description: form.value.description
    }

    if (isEdit.value) {
      await updateProduct(productId.value, data)
      showToast('保存成功')
    } else {
      await createProduct(data)
      showToast('创建成功')
    }
    router.replace('/mobile/operator/products')
  } catch (e: any) {
    console.error('Submit error:', e)
  }
}
</script>

<template>
  <div class="product-form-page">
    <van-nav-bar
      :title="isEdit ? '编辑商品' : '新增商品'"
      left-arrow
      @click-left="router.back()"
    />
    <div class="form-content">
      <van-cell-group inset>
        <van-field v-model="form.name" label="商品名称" placeholder="请输入商品名称" required />
        <select-picker
          v-model="form.categoryId"
          :options="categoryOptions"
          label="商品分类"
          placeholder="请选择分类"
        />
        <van-cell title="发货类型" required>
          <template #value>
            <van-radio-group v-model="form.type" direction="horizontal">
              <van-radio name="1" icon-size="16px">实体</van-radio>
              <van-radio name="2" icon-size="16px">虚拟</van-radio>
            </van-radio-group>
          </template>
        </van-cell>
        <van-field v-model="form.price" type="number" label="积分价格" placeholder="请输入积分" required />
        <van-field v-model="form.stock" type="number" label="库存" placeholder="请输入库存" required />
        <van-field v-model="form.limitPerUser" type="number" label="单人限购" placeholder="0表示不限购" required />
      </van-cell-group>

      <image-uploader
        v-model="form.mainImage"
        :multiple="false"
        label="商品主图"
        style="margin-top: 12px;"
      />

      <image-uploader
        v-model="form.detailImages"
        :multiple="true"
        :max-count="10"
        label="详情图"
        style="margin-top: 12px;"
      />

      <van-cell-group inset style="margin-top: 12px;">
        <van-field v-model="form.description" type="textarea" label="商品描述" placeholder="请输入描述" rows="3" />
      </van-cell-group>

      <div class="submit-wrap">
        <van-button type="primary" block round @click="handleSubmit">{{ submitText }}</van-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-form-page {
  background: var(--bg-primary);
  min-height: 100vh;
  height: 100dvh;
}

.form-content {
  padding: 16px 0;
  padding-bottom: 80px;
}

.submit-wrap {
  margin: 24px 16px;
}
</style>