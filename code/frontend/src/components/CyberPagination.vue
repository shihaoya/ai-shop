<template>
  <div class="cyber-pagination">
    <span class="total-text">共 {{ total }} 条</span>

    <div class="page-controls">
      <button
        class="page-btn"
        :class="{ disabled: current <= 1 }"
        :disabled="current <= 1"
        @click="goTo(current - 1)"
      >
        <i class="fas fa-chevron-left"></i>
      </button>

      <template v-for="item in visiblePages" :key="item">
        <button
          v-if="item !== '...'"
          class="page-btn"
          :class="{ active: item === current }"
          @click="goTo(item)"
        >
          {{ item }}
        </button>
        <span v-else class="ellipsis">...</span>
      </template>

      <button
        class="page-btn"
        :class="{ disabled: current >= totalPages }"
        :disabled="current >= totalPages"
        @click="goTo(current + 1)"
      >
        <i class="fas fa-chevron-right"></i>
      </button>
    </div>

    <div class="size-selector" v-if="showSizeChanger && pageSizeOptions.length > 0" ref="sizeSelectorRef">
      <div class="size-trigger" @click="showSizeDropdown = !showSizeDropdown">
        <span>{{ internalPageSize }}条/页</span>
        <i class="fas fa-chevron-down" :class="{ open: showSizeDropdown }"></i>
      </div>
      <div class="size-dropdown" v-if="showSizeDropdown">
        <div
          v-for="size in pageSizeOptions"
          :key="size"
          class="size-option"
          :class="{ active: Number(size) === internalPageSize }"
          @click="selectSize(size)"
        >
          <span>{{ size }}条/页</span>
          <i v-if="Number(size) === internalPageSize" class="fas fa-check"></i>
        </div>
      </div>
    </div>

    <div class="quick-jumper" v-if="showQuickJumper">
      <span>跳至</span>
      <input
        type="number"
        v-model="jumpPage"
        :min="1"
        :max="totalPages"
        autocomplete="off"
        @keyup.enter="doJump"
      />
      <span>页</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue'

const props = withDefaults(defineProps<{
  current: number
  pageSize: number
  total: number
  pageSizeOptions?: string[]
  showSizeChanger?: boolean
  showQuickJumper?: boolean
}>(), {
  pageSizeOptions: () => ['10', '20', '50', '100'],
  showSizeChanger: true,
  showQuickJumper: true
})

const emit = defineEmits<{
  'update:current': [page: number]
  'update:pageSize': [size: number]
  'change': [page: number, size: number]
}>()

const internalPageSize = ref(props.pageSize)
const jumpPage = ref('')
const showSizeDropdown = ref(false)
const sizeSelectorRef = ref<HTMLElement | null>(null)

function onClickOutside(e: MouseEvent) {
  if (sizeSelectorRef.value && !sizeSelectorRef.value.contains(e.target as Node)) {
    showSizeDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', onClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onClickOutside)
})

watch(() => props.pageSize, (val) => {
  internalPageSize.value = val
})

const totalPages = computed(() => Math.ceil(props.total / props.pageSize) || 1)

const visiblePages = computed(() => {
  const pages: (number | string)[] = []
  const total = totalPages.value
  const current = props.current

  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    pages.push(1)

    if (current <= 4) {
      pages.push(2, 3, 4, 5, '...')
    } else if (current >= total - 3) {
      pages.push('...', total - 4, total - 3, total - 2, total - 1, total)
    } else {
      pages.push('...')
      pages.push(current - 1, current, current + 1)
      pages.push('...', total)
    }
  }

  return pages
})

function goTo(page: number | string) {
  if (typeof page !== 'number' || page < 1 || page > totalPages.value || page === props.current) return
  emit('update:current', page)
  emit('change', page, props.pageSize)
}

function selectSize(size: string) {
  const numSize = Number(size)
  internalPageSize.value = numSize
  showSizeDropdown.value = false
  emit('update:pageSize', numSize)
  emit('update:current', 1)
  emit('change', 1, numSize)
}

function doJump() {
  const page = parseInt(jumpPage.value, 10)
  if (page && page >= 1 && page <= totalPages.value) {
    goTo(page)
    jumpPage.value = ''
  }
}
</script>

<style scoped>
.cyber-pagination {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.total-text {
  font-size: 13px;
  color: var(--text-muted);
  font-family: var(--font-mono);
  margin-right: 8px;
}

.page-controls {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-btn {
  min-width: 34px;
  height: 34px;
  padding: 0 8px;
  border-radius: var(--radius-xs);
  border: 1px solid var(--border-subtle);
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.page-btn:hover:not(.disabled) {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--bg-card-hover);
}

.page-btn.active {
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  border-color: transparent;
  color: white;
  box-shadow: var(--accent-glow);
}

.page-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.ellipsis {
  color: var(--text-muted);
  padding: 0 4px;
}

.size-selector {
  position: relative;
  display: flex;
  align-items: center;
  z-index: 10;
}

.size-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: var(--radius-xs);
  border: 1px solid var(--border-subtle);
  background: var(--bg-card);
  cursor: pointer;
  user-select: none;
  transition: all 0.2s ease;
}

.size-trigger:hover {
  border-color: var(--accent);
}

.size-trigger span {
  font-size: 13px;
  color: var(--text-primary);
  white-space: nowrap;
}

.size-trigger i {
  font-size: 10px;
  color: var(--text-muted);
  transition: transform 0.2s ease;
}

.size-trigger i.open {
  transform: rotate(180deg);
}

.size-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  min-width: 120px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-xs);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  animation: dropdown-in 0.15s ease;
}

@keyframes dropdown-in {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.size-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.size-option span {
  font-size: 13px;
  color: var(--text-primary);
}

.size-option i {
  font-size: 12px;
  color: var(--accent);
}

.size-option:hover {
  background: rgba(var(--accent-rgb), 0.08);
}

.size-option.active {
  background: rgba(var(--accent-rgb), 0.12);
  font-weight: 500;
}

.size-option:not(:last-child) {
  border-bottom: 1px solid var(--border-subtle);
}

.quick-jumper {
  display: flex;
  align-items: center;
  gap: 6px;
}

.quick-jumper span {
  font-size: 13px;
  color: var(--text-muted);
}

.quick-jumper input {
  width: 50px;
  padding: 6px 8px;
  border-radius: var(--radius-xs);
  border: 1px solid var(--border-subtle);
  background: var(--bg-card);
  color: var(--text-primary);
  font-size: 13px;
  text-align: center;
  transition: all 0.2s ease;
}

.quick-jumper input:hover {
  border-color: var(--accent);
}

.quick-jumper input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.1);
}

/* Remove number input spinners */
.quick-jumper input::-webkit-outer-spin-button,
.quick-jumper input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.quick-jumper input[type=number] {
  -moz-appearance: textfield;
}
</style>