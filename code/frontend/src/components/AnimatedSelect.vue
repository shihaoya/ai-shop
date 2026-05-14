<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'

interface SelectOption {
  label: string
  value: string | number
}

const props = withDefaults(defineProps<{
  modelValue?: string | number
  options?: SelectOption[]
  placeholder?: string
  disabled?: boolean
  searchable?: boolean
}>(), {
  modelValue: '',
  options: () => [],
  placeholder: '请选择',
  disabled: false,
  searchable: false,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number): void
}>()

// visible = 控制 DOM 是否存在, animating = 控制动画方向
const visible = ref(false)
const leaving = ref(false)
const wrapperRef = ref<HTMLElement | null>(null)
const listRef = ref<HTMLElement | null>(null)
const searchQuery = ref('')
const focusedIdx = ref(-1)

const selectedOption = computed(() => {
  return props.options.find(opt => opt.value === props.modelValue) || null
})

const filteredOptions = computed(() => {
  if (!props.searchable || !searchQuery.value) return props.options
  const q = searchQuery.value.toLowerCase()
  return props.options.filter(opt => opt.label.toLowerCase().includes(q))
})

function toggle() {
  if (props.disabled) return

  if (!visible.value) {
    // 打开
    leaving.value = false
    visible.value = true
    searchQuery.value = ''
    focusedIdx.value = -1
    nextTick(() => {
      const input = wrapperRef.value?.querySelector('.select-search-input') as HTMLInputElement
      input?.focus()
    })
  } else {
    // 关闭
    startLeave()
  }
}

function select(opt: SelectOption) {
  emit('update:modelValue', opt.value)
  // 延迟关闭，让 emit 引起的父组件重渲染先完成，再开始离开动画
  startLeave()
}

function startLeave() {
  if (!visible.value || leaving.value) return
  leaving.value = true
  // 等 120ms 动画播放完再移除 DOM
  setTimeout(() => {
    visible.value = false
    leaving.value = false
  }, 120)
}

function handleTriggerKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' || e.key === ' ' || e.key === 'ArrowDown') {
    e.preventDefault()
    if (!visible.value) toggle()
  }
  if (e.key === 'Escape' && visible.value) {
    e.preventDefault()
    startLeave()
  }
}

function handleDropdownKeydown(e: KeyboardEvent) {
  const items = filteredOptions.value
  if (!items.length) return

  switch (e.key) {
    case 'Escape':
      e.preventDefault()
      startLeave()
      break
    case 'ArrowDown':
      e.preventDefault()
      focusedIdx.value = focusedIdx.value < items.length - 1 ? focusedIdx.value + 1 : 0
      scrollToItem()
      break
    case 'ArrowUp':
      e.preventDefault()
      focusedIdx.value = focusedIdx.value > 0 ? focusedIdx.value - 1 : items.length - 1
      scrollToItem()
      break
    case 'Enter':
      e.preventDefault()
      if (focusedIdx.value >= 0 && focusedIdx.value < items.length) {
        select(items[focusedIdx.value])
      }
      break
    case 'Tab':
      startLeave()
      break
  }
}

function scrollToItem() {
  nextTick(() => {
    if (!listRef.value) return
    const el = listRef.value.children[focusedIdx.value] as HTMLElement | undefined
    el?.scrollIntoView({ block: 'nearest' })
  })
}

function handleDocumentClick(e: MouseEvent) {
  if (!visible.value || leaving.value) return
  if (wrapperRef.value && !wrapperRef.value.contains(e.target as Node)) {
    startLeave()
  }
}

onMounted(() => {
  document.addEventListener('click', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick)
})
</script>

<template>
  <div
    ref="wrapperRef"
    class="animated-select-wrapper"
    :class="{ disabled: disabled, open: visible }"
  >
    <!-- Trigger -->
    <div
      class="select-trigger"
      role="combobox"
      :aria-expanded="visible"
      :aria-disabled="disabled"
      tabindex="0"
      @click.stop="toggle"
      @keydown="handleTriggerKeydown"
    >
      <span class="select-value" :class="{ placeholder: !selectedOption }">
        {{ selectedOption ? selectedOption.label : placeholder }}
      </span>
      <span class="select-arrow" :class="{ open: visible }">
        <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
          <path d="M3 5L6 8L9 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </span>
    </div>

    <!-- Dropdown (手动动画控制，不用 Transition 组件避免闪烁) -->
    <div
      v-if="visible"
      class="select-dropdown"
      :class="{ 'dropdown-leave': leaving }"
      role="listbox"
      @keydown="handleDropdownKeydown"
    >
      <!-- Search -->
      <div v-if="searchable" class="select-search" @click.stop>
        <svg class="search-icon" width="14" height="14" viewBox="0 0 14 14" fill="none">
          <circle cx="6.5" cy="6.5" r="4.5" stroke="currentColor" stroke-width="1.3"/>
          <path d="M10 10L13 13" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
        </svg>
        <input
          v-model="searchQuery"
          class="select-search-input"
          type="text"
          placeholder="搜索..."
        />
      </div>

      <!-- Options -->
      <div ref="listRef" class="select-options">
        <div
          v-for="(opt, idx) in filteredOptions"
          :key="opt.value"
          class="select-option"
          :class="{
            selected: opt.value === modelValue,
            focused: idx === focusedIdx
          }"
          role="option"
          :aria-selected="opt.value === modelValue"
          @click.stop="select(opt)"
          @mouseenter="focusedIdx = idx"
        >
          <span class="option-label">{{ opt.label }}</span>
          <span v-if="opt.value === modelValue" class="option-check">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M3 7.5L5.5 10L11 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </span>
        </div>
        <div v-if="filteredOptions.length === 0" class="select-no-data">
          无匹配选项
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animated-select-wrapper {
  position: relative;
  width: 100%;
  font-size: 13px;
}

.animated-select-wrapper.disabled {
  opacity: 0.4;
  pointer-events: none;
}

/* ===== Trigger ===== */
.select-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 9px 12px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-input);
  color: var(--text-primary);
  border-radius: var(--radius);
  cursor: pointer;
  user-select: none;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 40px;
}

.select-trigger:hover {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.06), inset 0 0 10px rgba(var(--accent-rgb), 0.02);
}

.select-trigger:focus-visible {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.10), inset 0 0 10px rgba(var(--accent-rgb), 0.03);
}

.animated-select-wrapper.open .select-trigger {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.10), inset 0 0 10px rgba(var(--accent-rgb), 0.03);
}

.select-value {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
}

.select-value.placeholder {
  color: var(--text-muted);
}

/* ===== Arrow ===== */
.select-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.select-arrow.open {
  transform: rotate(180deg);
  color: var(--accent);
}

/* ===== Dropdown（手动动画） ===== */
.select-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  z-index: 100;
  background: var(--bg-card);
  border: 1px solid var(--border-glow);
  border-radius: var(--radius-sm);
  box-shadow: var(--accent-glow), 0 12px 40px rgba(0, 0, 0, 0.35);
  overflow: hidden;
  transform-origin: top center;
  backdrop-filter: blur(20px);
  animation: dropdownIn 0.18s cubic-bezier(0.4, 0, 0.2, 1);
}

.select-dropdown.dropdown-leave {
  animation: dropdownOut 0.12s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

/* ===== Search ===== */
.select-search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border-subtle);
}

.search-icon {
  flex-shrink: 0;
  color: var(--text-muted);
}

.select-search-input {
  flex: 1;
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-size: 13px;
  outline: none;
  font-family: inherit;
}

.select-search-input::placeholder {
  color: var(--text-muted);
}

/* ===== Options List ===== */
.select-options {
  max-height: 240px;
  overflow-y: auto;
  padding: 4px;
}

.select-options::-webkit-scrollbar {
  width: 4px;
}

.select-options::-webkit-scrollbar-track {
  background: transparent;
}

.select-options::-webkit-scrollbar-thumb {
  background: rgba(var(--accent-rgb), 0.2);
  border-radius: 2px;
}

/* ===== Option Item ===== */
.select-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 12px;
  border-radius: var(--radius-xs);
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.12s ease;
  user-select: none;
}

.select-option:hover,
.select-option.focused {
  background: rgba(var(--accent-rgb), 0.08);
  color: var(--text-primary);
}

.select-option.selected {
  color: var(--accent);
  background: rgba(var(--accent-rgb), 0.06);
  font-weight: 500;
}

.select-option.selected:hover,
.select-option.selected.focused {
  background: rgba(var(--accent-rgb), 0.12);
}

.option-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.option-check {
  display: flex;
  align-items: center;
  margin-left: 8px;
  flex-shrink: 0;
}

/* ===== No Data ===== */
.select-no-data {
  padding: 24px 12px;
  text-align: center;
  color: var(--text-muted);
  font-size: 12px;
}

/* ===== Animation Keyframes ===== */
@keyframes dropdownIn {
  from {
    opacity: 0;
    transform: translateY(-4px) scaleY(0.94);
  }
  to {
    opacity: 1;
    transform: translateY(0) scaleY(1);
  }
}

@keyframes dropdownOut {
  from {
    opacity: 1;
    transform: translateY(0) scaleY(1);
  }
  to {
    opacity: 0;
    transform: translateY(-3px) scaleY(0.96);
  }
}
</style>
