<script setup lang="ts">
import { ref } from 'vue'
import { showToast } from 'vant'
import { uploadProductImage } from '@/api/file'

interface Props {
  modelValue: string | string[]
  multiple?: boolean
  maxCount?: number
  label?: string
  required?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  multiple: false,
  maxCount: 5,
  label: '',
  required: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | string[]]
}>()

const imageList = ref<string[]>(Array.isArray(props.modelValue) ? props.modelValue : (props.modelValue ? [props.modelValue] : []))

async function handleAfterRead(file: any) {
  if (!file.file) return
  try {
    const res = await uploadProductImage(file.file)
    if (props.multiple) {
      imageList.value.push(res.url)
      emit('update:modelValue', [...imageList.value])
    } else {
      imageList.value = [res.url]
      emit('update:modelValue', res.url)
    }
  } catch {
    showToast('上传失败')
  }
}

function handleRemove(index: number) {
  imageList.value.splice(index, 1)
  if (props.multiple) {
    emit('update:modelValue', [...imageList.value])
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
        <div v-for="(img, idx) in imageList" :key="idx" class="uploader-item">
          <van-image :src="img" width="80" height="80" fit="cover" radius="8" />
          <div class="remove-btn" @click="handleRemove(idx)">×</div>
        </div>
        <van-uploader
          v-if="imageList.length < maxCount"
          :after-read="handleAfterRead"
          :max-count="maxCount - imageList.length"
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
          <div v-if="imageList.length === 0" class="upload-placeholder">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            <span>点击上传</span>
          </div>
          <van-image v-else :src="imageList[0]" width="80" height="80" fit="cover" radius="8" />
        </van-uploader>
        <div v-if="imageList.length > 0" class="remove-single" @click="handleRemove(0)">×</div>
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