<script setup lang="ts">
import { ref, watch } from 'vue'
import { showToast } from 'vant'
import { uploadProductImage, getFilesByIds } from '@/api/file'

interface Props {
  modelValue: string | string[] | undefined | null
  multiple?: boolean
  maxCount?: number
  label?: string
  required?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  multiple: false,
  maxCount: 10,
  label: '',
  required: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | string[]]
}>()

// 显示用的 URL 列表
const imageUrls = ref<string[]>([])
// 实际提交的 ID 列表
const imageIds = ref<string[]>([])

// 同步外部值（外部传的是文件 ID）
watch(() => props.modelValue, async (val) => {
  if (props.multiple && Array.isArray(val) && val.length > 0) {
    imageIds.value = val.filter(Boolean)
    // 批量获取 URL
    const files = await getFilesByIds(imageIds.value)
    imageUrls.value = imageIds.value.map(id => {
      const file = files.find(f => f.id === id)
      return file?.url || ''
    }).filter(Boolean)
  } else if (!props.multiple && typeof val === 'string' && val) {
    imageIds.value = [val]
    const files = await getFilesByIds([val])
    imageUrls.value = files[0] ? [files[0].url] : []
  } else {
    imageUrls.value = []
    imageIds.value = []
  }
}, { immediate: true })

async function handleAfterRead(file: any) {
  if (!file.file) return
  try {
    const res = await uploadProductImage(file.file)
    const fileId = res.id as string
    const fileUrl = res.url as string

    if (props.multiple) {
      imageUrls.value.push(fileUrl)
      imageIds.value.push(fileId)
      emit('update:modelValue', [...imageIds.value])
    } else {
      imageUrls.value = [fileUrl]
      imageIds.value = [fileId]
      emit('update:modelValue', fileId)
    }
  } catch {
    showToast('上传失败')
  }
}

function handleRemove(index: number) {
  imageUrls.value.splice(index, 1)
  imageIds.value.splice(index, 1)
  if (props.multiple) {
    emit('update:modelValue', [...imageIds.value])
  } else {
    emit('update:modelValue', '')
  }
}
</script>

<template>
  <div class="image-uploader">
    <div v-if="label" class="uploader-label">
      <span v-if="required" class="required">*</span>
      {{ label }}
    </div>
    <div class="uploader-content">
      <template v-if="multiple">
        <div v-for="(img, idx) in imageUrls" :key="idx" class="uploader-item">
          <van-image :src="img" width="80" height="80" fit="cover" radius="8" />
          <div class="remove-btn" @click="handleRemove(idx)">×</div>
        </div>
        <van-uploader
          v-if="imageUrls.length < maxCount"
          :after-read="handleAfterRead"
          :max-count="maxCount - imageUrls.length"
          accept="image/*"
        >
          <div class="upload-placeholder">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            <span>点击上传</span>
          </div>
        </van-uploader>
      </template>
      <template v-else>
        <van-uploader
          :after-read="handleAfterRead"
          :max-count="1"
          accept="image/*"
        >
          <div v-if="imageUrls.length === 0" class="upload-placeholder">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            <span>点击上传</span>
          </div>
          <van-image v-else :src="imageUrls[0]" width="80" height="80" fit="cover" radius="8" />
        </van-uploader>
        <div v-if="imageUrls.length > 0" class="remove-single" @click="handleRemove(0)">×</div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.image-uploader {
  background: var(--bg-card);
  margin: 0 16px;
  border-radius: 8px;
  padding: 12px 16px;
}

.uploader-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.uploader-label .required {
  color: #ee0a24;
  margin-right: 2px;
}

.uploader-content {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.uploader-item {
  position: relative;
}

.remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-single {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  border: 1px dashed var(--border-subtle);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: var(--text-muted);
  font-size: 12px;
  background: var(--bg-primary);
}
</style>