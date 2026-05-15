<script setup lang="ts">
import { ref, watch, shallowRef } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { Upload } from 'ant-design-vue'
import { uploadFile, deleteFile, getFile } from '@/api/upload'

const props = defineProps<{
  modelValue: string[]
  businessType: string
  maxCount?: number
}>()

const emit = defineEmits<{
  'update:modelValue': [val: string[]]
}>()

const fileList = shallowRef<any[]>([])
const previewVisible = ref(false)
const previewImage = ref('')
let suppressCount = 0  // 计数：>0 时 watch 不处理

// 外部 modelValue 变化时，加载文件信息
watch(() => props.modelValue, async (ids) => {
  if (suppressCount > 0) return
  if (!ids || ids.length === 0) {
    fileList.value = []
    return
  }
  // 过滤掉无效值，避免 'undefined'/'null' 被当作有效ID
  const validIds = ids.filter(id => id && id !== 'undefined' && id !== 'null')
  const newList: any[] = []
  const existingIds = new Set(fileList.value.map(f => String(f.response)))
  for (const id of validIds) {
    const sid = String(id)
    if (existingIds.has(sid)) continue
    try {
      const record = await getFile(id)
      newList.push({ uid: sid, name: record.fileName, status: 'done', url: record.url, response: sid })
    } catch {
      newList.push({ uid: sid, name: sid, status: 'done', url: '', response: sid })
    }
  }
  if (newList.length > 0) {
    suppressCount++
    fileList.value = [...fileList.value, ...newList]
    suppressCount--
  }
}, { immediate: true })

function getBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = reject
  })
}

async function handlePreview(file: any) {
  if (!file.url && !file.preview) {
    file.preview = await getBase64(file.originFileObj)
  }
  previewImage.value = file.url || file.preview
  previewVisible.value = true
}

async function customRequest(options: any) {
  const { file, onError } = options
  try {
    const res = await uploadFile(file, props.businessType)
    if (!res.id) throw new Error('上传返回缺少id')
    suppressCount++
    // 找到 Upload 自动添加的临时条目，原地替换为永久条目
    // 不调用 onSuccess —— 我们自己管理 fileList，避免 Upload 同步回临时条目导致重复
    const idx = fileList.value.findIndex((f: any) => f.originFileObj === file)
    const newEntry = {
      uid: res.id,
      name: res.fileName,
      status: 'done',
      url: res.url,
      response: res.id,
    }
    if (idx >= 0) {
      const updated = [...fileList.value]
      updated[idx] = newEntry
      fileList.value = updated
    } else {
      fileList.value = [...fileList.value, newEntry]
    }
    emit('update:modelValue', fileList.value.map(f => f.response as string))
    suppressCount--
    message.success('上传成功')
  } catch (e: any) {
    suppressCount++
    // 上传失败时移除临时条目
    fileList.value = fileList.value.filter((f: any) => f.originFileObj !== file)
    emit('update:modelValue', fileList.value.map(f => f.response as string))
    suppressCount--
    message.error(e?.message || '上传失败')
    onError(e)
  }
}

async function handleRemove(file: any) {
  const id = file.response
  if (id) {
    try {
      await deleteFile(id)
    } catch { /* ignore */ }
  }
  suppressCount++
  fileList.value = fileList.value.filter(f => f.uid !== file.uid)
  emit('update:modelValue', fileList.value.map(f => f.response as string))
  suppressCount--
}
</script>

<template>
  <Upload
    v-model:fileList="fileList"
    listType="picture-card"
    :accept="'image/*'"
    :maxCount="maxCount || 1"
    :multiple="(maxCount || 1) > 1"
    :showUploadList="{ showPreviewIcon: true, showRemoveIcon: true }"
    :customRequest="customRequest"
    :remove="handleRemove"
    @preview="handlePreview"
  >
    <div v-if="fileList.length < (maxCount || 1)" class="upload-trigger">
      <PlusOutlined style="font-size: 20px;" />
      <div style="margin-top: 4px; font-size: 12px;">
        {{ maxCount && maxCount > 1 ? '上传图片' : '上传' }}
      </div>
    </div>
  </Upload>

  <Modal :open="previewVisible" :footer="null" @cancel="previewVisible = false">
    <img :src="previewImage" style="width: 100%;" />
  </Modal>
</template>

<style scoped>
:deep(.ant-upload-select-picture-card) {
  width: 96px !important;
  height: 96px !important;
}
.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #999;
}
</style>