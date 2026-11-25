import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/chat' },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('../views/Chat.vue'),
    meta: { title: 'RAG 问答' }
  },
  {
    path: '/doc',
    name: 'Doc',
    component: () => import('../views/DocManage.vue'),
    meta: { title: '文档管理' }
  },
  {
    path: '/agent',
    name: 'Agent',
    component: () => import('../views/Agent.vue'),
    meta: { title: 'Agent 工具调试台' }
  },
  {
    path: '/swagger',
    name: 'Swagger',
    component: () => import('../views/SwaggerUI.vue'),
    meta: { title: '接口文档' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
