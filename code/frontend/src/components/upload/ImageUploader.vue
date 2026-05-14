<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { Upload, Modal, Button, Progress, message } from 'ant-design-vue'
import { UploadOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import ImageCompressor from 'image-compressor.js'

interface Props {
  /** 绑定的文件ID */
  modelValue?: string | number
  /** 业务类型 */
  businessType?: string
  /** 业务ID */
  businessId?: string | number
  /** 是否禁用 */
  disabled?: boolean
  /** 上传按钮文字 */
  btnText?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  businessType: 'product',
  businessId: '',
  disabled: false,
  btnText: '上传图片',
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number): void
  (e: 'change', file: any): void
}>()

// 文件信息
const fileRecord = ref<any>(null)
const previewUrl = ref<string>('')
const uploading = ref(false)
const uploadProgress = ref(0)

// 裁剪弹窗
const cropVisible = ref(false)
const cropImage = ref('')
const cropCanvas = ref<HTMLCanvasElement | null>(null)

/** 计算属性：是否有文件 */
const hasFile = computed(() => !!props.modelValue)

/** 压缩后的Blob */
const compressedBlob = ref<Blob | null>(null)

/** 上传前的校验 */
function beforeUpload(file: File): boolean {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif']
  const maxSize = 10 * 1024 * 1024 // 10MB

  if (!allowedTypes.includes(file.type)) {
    message.error('只能上传 JPG/PNG/GIF 格式的图片')
    return false
  }
  if (file.size > maxSize) {
    message.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

/** 处理文件选择 */
async function handleChange(info: any) {
  const file = info.file.originFileObj || info.file
  if (!file) return

  // 先压缩（质量85%）
  const compressed = await compressImage(file)
  compressedBlob.value = compressed

  // 打开裁剪
  const reader = new FileReader()
  reader.onload = (e) => {
    cropImage.value = e.target?.result as string
    cropVisible.value = true
  }
  reader.readAsDataURL(compressed)
}

/** 压缩图片 */
async function compressImage(file: File): Promise<File> {
  return new Promise((resolve, reject) => {
    new ImageCompressor(file, {
      quality: 0.85,
      maxWidth: 1920,
      maxHeight: 1920,
      success(result) {
        resolve(result as File)
      },
      error(e) {
        reject(e)
      },
    })
  })
}

/** 确认裁剪 */
async function confirmCrop() {
  if (!cropCanvas.value) return

  const canvas = cropCanvas.value
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  // 获取图像实际尺寸
  const img = new Image()
  img.onload = async () => {
    // 设置canvas尺寸为图像尺寸
    canvas.width = img.width
    canvas.height = img.height
    ctx.drawImage(img, 0, 0)

    // 转为Blob
    const blob = await new Promise<Blob>((resolve) => {
      canvas.toBlob((b) => resolve(b!), 'image/jpeg', 0.85)
    })

    cropVisible.value = false

    // 再次压缩确保最小
    const finalFile = await compressImage(blob)
    await upload(finalFile)
  }
  img.src = cropImage.value
}

/** 上传文件 */
async function upload(file: File) {
  if (uploading.value) return

  uploading.value = true
  uploadProgress.value = 0

  try {
    const { uploadFile } = await import('@/api/upload')

    const res = await uploadFile(file, props.businessType, props.businessId ? Number(props.businessId) : undefined)
    fileRecord.value = res
    previewUrl.value = res.url
    uploadProgress.value = 100

    emit('update:modelValue', res.id)
    emit('change', res)

    message.success('上传成功')
  } catch (error: any) {
    message.error(error.message || '上传失败')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

/** 删除文件 */
async function handleDelete() {
  if (!props.modelValue) return

  try {
    const { deleteFile } = await import('@/api/upload')
    await deleteFile(props.modelValue)

    fileRecord.value = null
    previewUrl.value = ''
    emit('update:modelValue', '')
    emit('change', null)

    message.success('删除成功')
  } catch (error: any) {
    message.error(error.message || '删除失败')
  }
}

/** 预览图片 */
function handlePreview() {
  if (previewUrl.value) {
    previewVisible.value = true
  }
}

// 预览弹窗
const previewVisible = ref(false)

/** 加载已有文件 */
watch(
  () => props.modelValue,
  async (id) => {
    if (id) {
      try {
        const { getFile } = await import('@/api/upload')
        const res = await getFile(id)
        fileRecord.value = res
        previewUrl.value = res.url
      } catch (e) {
        // 忽略
      }
    } else {
      fileRecord.value = null
      previewUrl.value = ''
    }
  },
  { immediate: true }
)
</script>

<template>
  <div class="image-uploader">
    <!-- 已上传状态 -->
    <div v-if="hasFile" class="preview-container">
      <img v-if="previewUrl" :src="previewUrl" class="preview-image" @click="handlePreview" />
      <div class="preview-actions">
        <Button size="small" @click="handlePreview">
          <template #icon><UploadOutlined /></template>
          预览
        </Button>
        <Button size="small" danger @click="handleDelete" :disabled="disabled">
          <template #icon><DeleteOutlined /></template>
          删除
        </Button>
      </div>
      <div v-if="fileRecord" class="file-info">
        {{ fileRecord.fileName }} ({{ (fileRecord.fileSize / 1024).toFixed(1) }}KB)
      </div>
    </div>

    <!-- 上传状态 -->
    <div v-else class="upload-container">
      <Upload
        :before-upload="beforeUpload"
        :show-upload-list="false"
        :disabled="disabled || uploading"
        accept="image/jpeg,image/png,image/gif"
        @change="handleChange"
      >
        <Button type="primary" :loading="uploading" :disabled="disabled">
          <template #icon><UploadOutlined /></template>
          {{ uploading ? `上传中 ${uploadProgress}%` : btnText }}
        </Button>
      </Upload>
      <Progress v-if="uploading" :percent="uploadProgress" size="small" :show-info="false" />
    </div>

    <!-- 裁剪弹窗 -->
    <Modal
      v-model:open="cropVisible"
      title="裁剪图片"
      width="700px"
      :footer="null"
      destroy-on-close
    >
      <div class="crop-wrapper">
        <img :src="cropImage" class="crop-image" />
      </div>
      <div class="crop-tip">图片已压缩（质量85%），确认后上传</div>
      <div class="crop-actions">
        <Button @click="cropVisible = false">取消</Button>
        <Button type="primary" @click="confirmCrop">
          确认上传
        </Button>
      </div>
    </Modal>

    <!-- 预览弹窗 -->
    <Modal
      v-model:open="previewVisible"
      title="图片预览"
      :footer="null"
      width="800px"
    >
      <div style="text-align: center">
        <img :src="previewUrl" style="max-width: 100%" />
      </div>
    </Modal>
  </div>
</template>

<style scoped lang="scss">
.image-uploader {
  width: 100%;
}

.preview-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-image {
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #d9d9d9;
  cursor: pointer;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.file-info {
  font-size: 12px;
  color: #999;
}

.upload-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.crop-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
}

.crop-image {
  max-width: 100%;
  max-height: 400px;
}

.crop-tip {
  text-align: center;
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}

.crop-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>