<template>
  <div class="swagger-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>📖 接口文档（Swagger UI）</span>
        </div>
      </template>

      <div class="toolbar">
        <el-input
          v-model="swaggerUrl"
          placeholder="Swagger UI 地址"
          style="flex: 1"
          @keyup.enter="reload"
        />
        <el-button type="primary" @click="reload">加载</el-button>
        <el-button @click="useDefault">恢复默认</el-button>
      </div>

      <div class="iframe-wrap">
        <iframe
          v-if="iframeReady"
          :src="currentUrl"
          frameborder="0"
          class="swagger-iframe"
          @load="onIframeLoad"
        ></iframe>
        <div v-else class="iframe-loading">
          <el-icon class="is-loading" :size="24"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const DEFAULT_URL = '/swagger-ui.html'
const swaggerUrl = ref(DEFAULT_URL)
const iframeReady = ref(false)

const currentUrl = computed(() => {
  const u = swaggerUrl.value.trim()
  // 如果是绝对 URL（http/https），直接用；否则拼 /swagger-ui.html 或保持原样
  if (/^https?:\/\//i.test(u)) return u
  return u || DEFAULT_URL
})

onMounted(() => {
  // 默认立即加载（相对路径，走 Vite proxy）
  iframeReady.value = true
})

function reload() {
  iframeReady.value = false
  // 强制 iframe 重建
  setTimeout(() => {
    iframeReady.value = true
  }, 50)
}

function useDefault() {
  swaggerUrl.value = DEFAULT_URL
  reload()
}

function onIframeLoad() {
  iframeReady.value = true
}
</script>

<style scoped>
.swagger-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
}
.card-header {
  font-weight: 600;
}
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.iframe-wrap {
  flex: 1;
  background: #f5f7fa;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}
.swagger-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: #fff;
}
.iframe-loading {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}
</style>
