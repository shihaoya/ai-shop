import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSidebarStore = defineStore('sidebar', () => {
  const collapsed = ref(false)

  function toggle() {
    collapsed.value = !collapsed.value
    document.documentElement.style.setProperty(
      '--sidebar-width',
      collapsed.value ? '52px' : '200px'
    )
  }

  return { collapsed, toggle }
})