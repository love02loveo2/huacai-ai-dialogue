<template>
  <router-view />
  <!-- 看板娘组件（Hijiki 模型） -->
  <Live2D />



</template>

<script setup>
import { onMounted, nextTick } from 'vue'
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'
import Live2D from '@/components/Live2D.vue'


// 组件挂载后初始化主题样式
onMounted(async () => {
  await nextTick() // 确保 DOM 渲染完成后再执行
  handleThemeStyle(useSettingsStore().theme)
})
</script>


