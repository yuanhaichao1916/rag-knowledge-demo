遵循项目基线 prompt-base.md 中的「前端约定」。

本轮任务：搭建 Vue3 + Vite + Element Plus 前端脚手架（位于 frontend/）

要求：
1. 在仓库根目录创建 frontend/ 子目录，**不要**放到 src/ 下
2. 产出文件清单：
   - frontend/package.json：Vue 3.4、Vite 5、Element Plus 2.7、vue-router 4、axios、sse.js（SSE 客户端）
   - frontend/vite.config.js：dev server 监听 5173，proxy 把 /api 和 /v3 转发到 http://localhost:8080
   - frontend/index.html：id=app 的挂载点，中文 title
   - frontend/src/main.js：createApp + Element Plus + router
   - frontend/src/App.vue：带侧边栏布局（菜单：问答 / 文档管理 / Agent 调试 / 接口文档）
   - frontend/src/router/index.js：4 条路由，懒加载页面组件
   - frontend/src/assets/style.css：基础样式（Element Plus 主题定制可选，先默认）
   - frontend/.gitignore：node_modules、dist
3. **不**执行 npm install（留给用户在 IDEA 或终端操作，避免沙箱下载慢/失败）
4. 输出文件时每段代码前标注文件名
5. 不写任何页面业务逻辑（纯骨架）
