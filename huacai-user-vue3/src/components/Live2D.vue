<template>
  <div id="live2d-container" />
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  const script = document.createElement('script')
  script.src = 'https://cdn.jsdelivr.net/npm/live2d-widget@3.1.4/lib/L2Dwidget.min.js'
  script.onload = () => {
    window.L2Dwidget.init({
      model: {
        jsonPath: 'https://unpkg.com/live2d-widget-model-shizuku@1.0.5/assets/shizuku.model.json',
      },
      display: {
        position: 'right',
        width: 150,
        height: 300,
        hOffset: 0,
        vOffset: -20,
      },
      mobile: {
        show: true,
        scale: 0.5,
        motion: true,
      },
      react: {
        opacityDefault: 0.8,
        opacityOnHover: 1,
      },
    })
  }
  document.body.appendChild(script)
})
</script>

<style scoped>
#live2d-container {
  position: fixed;
  bottom: 0;
  right: 0;
  z-index: 999;
}
</style>