<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { createProduct, getCategories } from '@/api/product'
import ImageUploader from '@/components/ImageUploader.vue'
import SelectPicker from '@/components/SelectPicker.vue'
import type { Category } from '@/types'

const router = useRouter()

const loading = ref(false)
const categories = ref<Category[]>([])

const form = ref({
  name: '',
  categoryId: '',
  type: 1 as number,
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

onMounted(async () => {
  try {
    categories.value = await getCategories()
  } catch {
    // ignore
  }
})

async function handleSubmit() {
  if (!form.value.name.trim()) {
    showToast('请输入商品名称')
    return
  }
  if (!form.value.categoryId) {
    showToast('请选择商品分类')
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
  if (!form.value.mainImage) {
    showToast('请上传商品主图')
    return
  }
  try {
    await createProduct({
      name: form.value.name,
      categoryId: form.value.categoryId,
      type: form.value.type,
      price: form.value.price,
      stock: form.value.stock,
      limitPerUser: form.value.limitPerUser,
      mainImage: form.value.mainImage,
      detailImages: form.value.detailImages.join(','),
      description: form.value.description
    })
    showToast('创建成功')
    router.back()
  } catch {
    showToast('创建失败')
  }
}
</script>

<template>
  <div class="product-add-page">
    <van-nav-bar title="新增商品" left-arrow @click-left="router.back()" />
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
        :required="true"
        style="margin-top: 12px;"
      />

      <image-uploader
        v-model="form.detailImages"
        :multiple="true"
        :max-count="5"
        label="详情图"
        style="margin-top: 12px;"
      />

      <van-cell-group inset style="margin-top: 12px;">
        <van-field v-model="form.description" type="textarea" label="商品描述" placeholder="请输入描述" rows="3" />
      </van-cell-group>

      <div class="submit-wrap">
        <van-button type="primary" block round @click="handleSubmit">创建</van-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-add-page {
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