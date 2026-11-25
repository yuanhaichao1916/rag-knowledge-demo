遵循项目基线 prompt-base.md 中的「前端约定」。

本轮任务：封装统一请求层 + SSE 客户端

产出文件：
1. frontend/src/utils/request.js
   - 基于 axios，baseURL 默认 ''（Vite proxy 转发）
   - 请求拦截器：从 localStorage 读取 userToken 可选加 header
   - 响应拦截器：统一解构后端返回的 {code, msg, data}，code != 200 时 Element Plus ElMessage.error 弹出 msg
   - 导出 get / post / del 便捷方法

2. frontend/src/utils/sse.js
   - 封装 EventSource，支持手动 close
   - 统一处理事件类型：sources / message / done / error
   - 错误时回调 + 自动 reconnect 开关

3. frontend/src/api/doc.js
   - upload(payload) → POST /api/doc/upload
   - listChunks(docId) → GET /api/doc/{docId}/chunks
   - remove(docId) → DELETE /api/doc/{docId}

4. frontend/src/api/chat.js
   - chat(payload) → POST /api/chat
   - chatStream({ question, topK }, callbacks) → GET /api/chat/stream，用 sse.js 封装

5. frontend/src/api/agent.js
   - chat(payload) → POST /api/agent/chat

所有 API 方法内部统一走 request.js / sse.js，页面组件不直接调 axios。
