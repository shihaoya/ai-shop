<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { Tabbar, TabbarItem } from 'vant'

const route = useRoute()

const tabList = [
  { name: 'products', title: '商品', icon: 'shop-o', path: '/mobile/operator/products' },
  { name: 'orders', title: '订单', icon: 'orders-o', path: '/mobile/operator/orders' },
  { name: 'users', title: '用户', icon: 'friends-o', path: '/mobile/operator/users' },
  { name: 'profile', title: '我的', icon: 'user-o', path: '/mobile/operator/profile' },
]

const active = computed(() => {
  const path = route.path
  const found = tabList.find(t => path.startsWith(t.path))
  return found ? tabList.indexOf(found) : 0
})
</script>

<template>
  <div class="operator-layout">
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
.operator-layout {
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