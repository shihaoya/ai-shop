<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { importUsers, downloadImportTemplate } from '@/api/operator'
import type { ImportResult } from '@/api/operator'
import * as XLSX from 'xlsx'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ close: []; success: [] }>()

// --- state ---
type Step = 'select' | 'uploading' | 'result'
const step = ref<Step>('select')
const result = ref<ImportResult | null>(null)
const isError = ref(false)
const dragOver = ref(false)

const fileInput = ref<HTMLInputElement>()

// --- reset on open ---
watch(() => props.visible, (v) => {
  if (v) reset()
})

function reset() {
  step.value = 'select'
  result.value = null
  isError.value = false
  dragOver.value = false
}

// --- file handling ---
function handleFile(file: File) {
  // validate extension
  const ext = file.name.split('.').pop()?.toLowerCase()
  if (ext !== 'xlsx' && ext !== 'xls') {
    message.warning('仅支持 .xlsx / .xls 格式')
    return
  }
  doUpload(file)
}

function onInputChange(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files?.length) {
    handleFile(target.files[0])
    target.value = '' // allow re-select same file
  }
}

function onDrop(e: DragEvent) {
  dragOver.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file) handleFile(file)
}

function onDragOver(e: DragEvent) {
  e.preventDefault()
  dragOver.value = true
}
function onDragLeave() {
  dragOver.value = false
}

// --- upload ---
async function doUpload(file: File) {
  step.value = 'uploading'
  try {
    const res = await importUsers(file)
    result.value = res
    if (res.hasErrors && res.errors?.length) {
      isError.value = true
    } else {
      isError.value = false
    }
    step.value = 'result'
  } catch (e: any) {
    message.error(e?.message || '导入失败')
    step.value = 'select'
  }
}

// --- actions ---
function close() {
  emit('close')
  if (!isError.value && result.value?.users?.length) {
    emit('success')
  }
}

function reUpload() {
  step.value = 'select'
  result.value = null
  isError.value = false
}

function downloadPwdExcel() {
  if (!result.value?.users?.length) return
  const data = [['用户名', '昵称', '密码'], ...result.value.users.map(u => [u.username, u.nickname, u.password])]
  const ws = XLSX.utils.aoa_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '用户密码')
  XLSX.writeFile(wb, '导入用户密码.xlsx')
}
</script>

<template>
  <div class="modal-overlay" v-if="props.visible" @click.self="close">
    <div class="modal-card">
      <!-- ====== 标题栏 ====== -->
      <div class="modal-header">
        <h3>
          <i :class="['title-icon', step === 'result' ? (isError ? 'err' : 'ok') : '']"></i>
          <template v-if="step === 'select'">导入用户</template>
          <template v-else-if="step === 'uploading'">正在导入</template>
          <template v-else-if="isError">导入结果</template>
          <template v-else>导入成功</template>
        </h3>
        <button class="modal-close" @click="close"><i class="fas fa-times"></i></button>
      </div>

      <div class="modal-body">
        <!-- ====== 步骤1: 选择文件 ====== -->
        <template v-if="step === 'select'">
          <p class="desc">
            选择填写好的 Excel 文件，自动开始导入。
            <a class="link" @click="downloadImportTemplate">下载模板</a>
          </p>

          <div
            class="drop-zone"
            :class="{ dragover: dragOver }"
            @click="fileInput?.click()"
            @drop.prevent="onDrop"
            @dragover.prevent="onDragOver"
            @dragleave="onDragLeave"
          >
            <input
              ref="fileInput"
              type="file"
              accept=".xlsx,.xls"
              style="display:none"
              @change="onInputChange"
            />
            <div class="drop-icon">
              <i class="fas fa-cloud-upload-alt"></i>
            </div>
            <p class="drop-text">点击选择文件，或将文件拖拽到此处</p>
            <p class="drop-hint">支持 .xlsx / .xls 格式</p>
          </div>
        </template>

        <!-- ====== 步骤2: 上传中 ====== -->
        <template v-if="step === 'uploading'">
          <div class="loading-box">
            <div class="spinner"></div>
            <p class="loading-text">正在解析并导入数据，请稍候…</p>
          </div>
        </template>

        <!-- ====== 步骤3: 结果展示 ====== -->
        <template v-if="step === 'result' && result">
          <!-- 异常 -->
          <template v-if="isError && result.errors?.length">
            <div class="result-banner err">
              <i class="fas fa-exclamation-circle"></i>
              <span>文件中有 <strong>{{ result.errors.length }}</strong> 行数据存在问题，请修改后重新上传</span>
            </div>
            <div class="table-wrap">
              <table class="result-table">
                <thead>
                  <tr>
                    <th class="col-row">行号</th>
                    <th>错误信息</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="err in result.errors" :key="err.row">
                    <td class="col-row">{{ err.row }}</td>
                    <td class="err-msg">{{ err.message }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <!-- 成功 -->
          <template v-if="!isError && result.users?.length">
            <div class="result-banner ok">
              <i class="fas fa-check-circle"></i>
              <span>成功导入 <strong>{{ result.users.length }}</strong> 个用户，已自动生成随机密码</span>
            </div>
            <div class="table-wrap">
              <table class="result-table">
                <thead>
                  <tr>
                    <th class="col-idx">#</th>
                    <th>用户名</th>
                    <th>昵称</th>
                    <th>密码</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(user, idx) in result.users" :key="idx">
                    <td class="col-idx">{{ idx + 1 }}</td>
                    <td>{{ user.username }}</td>
                    <td class="nick-cell">{{ user.nickname }}</td>
                    <td><code class="pwd-tag">{{ user.password }}</code></td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p class="pwd-note">请及时将密码分发给相应用户。</p>
          </template>
        </template>
      </div>

      <!-- ====== 底部按钮 ====== -->
      <div class="modal-footer">
        <template v-if="step === 'select'">
          <button class="cyber-btn" @click="close">取消</button>
        </template>
        <template v-if="step === 'uploading'">
          <button class="cyber-btn" disabled>导入中…</button>
        </template>
        <template v-if="step === 'result'">
          <button class="cyber-btn" @click="close">关闭</button>
          <button v-if="isError" class="cyber-btn-primary" @click="reUpload">
            <i class="fas fa-redo" style="margin-right:5px;"></i>重新上传
          </button>
          <button v-if="!isError" class="cyber-btn-primary" @click="downloadPwdExcel">
            <i class="fas fa-file-excel" style="margin-right:5px;"></i>下载 Excel
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ====== overlay & card ====== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(7, 8, 22, 0.8);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-card {
  background: var(--bg-card);
  border: 1px solid var(--border-glow);
  border-radius: var(--radius);
  width: 520px;
  max-width: 92vw;
  box-shadow: var(--accent-glow), 0 20px 50px rgba(0,0,0,0.4);
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(12px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

/* ====== header ====== */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  border-bottom: 1px solid var(--border-subtle);
}
.modal-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}
.title-icon {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
}
.title-icon.err { background: var(--danger); box-shadow: 0 0 8px rgba(239,68,68,0.5); }
.title-icon.ok  { background: var(--success); box-shadow: 0 0 8px rgba(34,197,94,0.5); }
.modal-close {
  padding: 4px 8px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  border-radius: var(--radius-xs);
  transition: all 0.2s;
}
.modal-close:hover {
  background: var(--bg-card-hover);
  color: var(--text-primary);
}

/* ====== body ====== */
.modal-body {
  padding: 24px;
}
.desc {
  margin-bottom: 16px;
  color: var(--text-secondary);
  font-size: 13px;
}
.link {
  color: var(--accent);
  cursor: pointer;
  text-decoration: none;
}
.link:hover { text-decoration: underline; }

/* ====== drop zone ====== */
.drop-zone {
  border: 2px dashed var(--border);
  border-radius: 10px;
  padding: 36px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.25s;
}
.drop-zone:hover {
  border-color: var(--accent);
  background: rgba(99,102,241,0.06);
}
.drop-zone.dragover {
  border-color: var(--accent);
  background: rgba(99,102,241,0.12);
  transform: scale(1.01);
}
.drop-icon i {
  font-size: 40px;
  color: var(--accent);
  margin-bottom: 10px;
}
.drop-text {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 4px;
}
.drop-hint {
  color: var(--text-muted);
  font-size: 12px;
}

/* ====== loading ====== */
.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
}
.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  margin-bottom: 16px;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.loading-text {
  color: var(--text-muted);
  font-size: 14px;
}

/* ====== result banner ====== */
.result-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 13px;
}
.result-banner.err {
  background: rgba(239,68,68,0.1);
  border: 1px solid rgba(239,68,68,0.25);
  color: #f87171;
}
.result-banner.ok {
  background: rgba(34,197,94,0.1);
  border: 1px solid rgba(34,197,94,0.25);
  color: #4ade80;
}
.result-banner i { font-size: 18px; }
.result-banner strong { font-weight: 700; }

/* ====== result table ====== */
.table-wrap {
  max-height: 360px;
  overflow: auto;
  border: 1px solid var(--border);
  border-radius: 8px;
}
.result-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.result-table thead {
  position: sticky;
  top: 0;
  z-index: 1;
}
.result-table th {
  background: var(--bg-secondary);
  padding: 10px 14px;
  text-align: left;
  color: var(--text-muted);
  font-weight: 500;
  border-bottom: 1px solid var(--border);
  white-space: nowrap;
}
.result-table td {
  padding: 10px 14px;
  border-bottom: 1px solid var(--border-subtle);
  color: var(--text-primary);
}
.result-table tbody tr:hover {
  background: rgba(99,102,241,0.04);
}
.result-table tbody tr:last-child td {
  border-bottom: none;
}
.col-row { width: 70px; text-align: center; font-family: var(--font-mono); color: var(--text-muted) !important; }
.col-idx { width: 44px; text-align: center; font-family: var(--font-mono); color: var(--text-muted) !important; }
.err-msg { color: var(--danger) !important; }
.nick-cell { color: var(--text-secondary) !important; }

/* ====== password tag ====== */
.pwd-tag {
  background: var(--bg-secondary);
  padding: 3px 10px;
  border-radius: 5px;
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--accent);
  font-weight: 600;
}

/* ====== password note ====== */
.pwd-note {
  margin-top: 12px;
  font-size: 12px;
  color: var(--text-muted);
}

/* ====== footer ====== */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 24px;
  border-top: 1px solid var(--border-subtle);
}

/* ====== shared button styles ====== */
.cyber-btn, .cyber-btn-primary {
  padding: 9px 18px;
  border-radius: var(--radius-xs);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.cyber-btn {
  background: var(--bg-card-hover);
  color: var(--text-secondary);
  border-color: var(--border);
}
.cyber-btn:hover { background: var(--bg-card-hover); color: var(--text-primary); border-color: var(--accent); }
.cyber-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.cyber-btn-primary {
  background: var(--accent);
  color: #fff;
  border-color: var(--accent);
}
.cyber-btn-primary:hover { filter: brightness(1.15); }
.cyber-btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
