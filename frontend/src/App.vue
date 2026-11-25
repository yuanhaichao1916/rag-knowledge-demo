<template>
  <el-container class="layout-root">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">🧠 RAG Demo</div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        router
        background-color="#1f2d3d"
        text-color="#b0bec5"
        active-text-color="#409eff"
      >
        <el-menu-item index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>RAG 问答</span>
        </el-menu-item>
        <el-menu-item index="/doc">
          <el-icon><Document /></el-icon>
          <span>文档管理</span>
        </el-menu-item>
        <el-menu-item index="/agent">
          <el-icon><MagicStick /></el-icon>
          <span>Agent 调试</span>
        </el-menu-item>
        <el-menu-item index="/swagger">
          <el-icon><Tickets /></el-icon>
          <span>接口文档</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <span class="header-title">{{ currentTitle }}</span>
      </el-header>
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ChatDotRound, Document, MagicStick, Tickets } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
const titleMap = {
  '/chat': 'RAG 问答',
  '/doc': '文档管理',
  '/agent': 'Agent 工具调试台',
  '/swagger': '接口文档'
}
const currentTitle = computed(() => titleMap[route.path] || 'RAG 知识库 Demo')
</script>

<style scoped>
.layout-root {
  height: 100vh;
}
.layout-aside {
  background-color: #1f2d3d;
  color: #fff;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  background-color: #1a252f;
}
.layout-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  line-height: 60px;
  padding: 0 24px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
}
.layout-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
