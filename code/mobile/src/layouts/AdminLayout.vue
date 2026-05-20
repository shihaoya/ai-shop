<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { Tabbar, TabbarItem } from 'vant'

const route = useRoute()

const tabList = [
  { name: 'shops', title: '店铺', icon: 'shop-o', path: '/mobile/admin/shops' },
  { name: 'users', title: '用户', icon: 'friends-o', path: '/mobile/admin/users' },
  { name: 'profile', title: '我的', icon: 'user-o', path: '/mobile/admin/profile' },
]

const active = computed(() => {
  const path = route.path
  const found = tabList.find(t => path.startsWith(t.path))
  return found ? tabList.indexOf(found) : 0
})
</script>

<template>
  <div class="admin-layout">
    <div class="page-content">
      <RouterView />
    </div>
    <Tabbar route fixed bottom safe-area-inset-bottom>
      <TabbarItem
        v-for="item in tabList"
        :key="item.name"
        :to="item.path"
      >
        {{ item.title }}
        <template #icon="props">
          <van-icon :name="item.icon" />
        </template>
      </TabbarItem>
    </Tabbar>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  background: var(--bg-primary);
}

.page-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
}
</style>