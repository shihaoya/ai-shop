<script setup lang="ts">
import { ref, computed } from 'vue'

export interface PickerOption {
  text: string
  value: string | number
}

interface Props {
  modelValue: string | number
  options: PickerOption[]
  label?: string
  placeholder?: string
  required?: boolean
  title?: string
}

const props = withDefaults(defineProps<Props>(), {
  label: '',
  placeholder: '请选择',
  required: false,
  title: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
}>()

const showPicker = ref(false)

const displayValue = computed(() => {
  const opt = props.options.find(o => String(o.value) === String(props.modelValue))
  return opt ? opt.text : ''
})

function openPicker() {
  showPicker.value = true
}

function onConfirm(p: any) {
  emit('update:modelValue', p.value)
  showPicker.value = false
}
</script>

<template>
  <van-field
    :model-value="displayValue"
    is-link
    readonly
    :label="label"
    :placeholder="placeholder"
    :required="required"
    @click="openPicker"
  />
  <van-popup v-model:show="showPicker" position="bottom">
    <van-picker
      :title="title"
      :columns="options"
      @confirm="onConfirm"
      @cancel="showPicker = false"
    />
  </van-popup>
</template>