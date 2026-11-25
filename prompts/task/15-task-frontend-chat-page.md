遵循项目基线 prompt-base.md 中的「前端约定」。

本轮任务：RAG 问答主界面（frontend/src/views/Chat.vue）

功能：
1. 左侧聊天面板：
   - 多行输入框（el-input type=textarea），发送按钮 +「流式」开关
   - 消息气泡左右分布（用户 / 助手），助手消息支持 Markdown 渲染（v-html 或 markdown-it，后者更安全）
2. 引用片段面板（右侧或气泡下方折叠）：
   - 展示 sources：title / content / score
   - 点击「引用」复制片段到剪贴板
3. SSE 流式逻辑：
   - 调用 api/chat.js 的 chatStream
   - 每收到 message 事件就 append 到当前回复气泡（打字机效果）
   - sources 事件到达后先渲染来源面板
   - done / error 事件结束 loading 状态
4. 空知识库兜底：后端返回【知识库未收录相关信息】时，前端用灰色样式高亮提示
5. topK 参数：输入框旁下拉选择 3/5/8/10，透传给后端

**注意**：后端 enable-rag=false 时会跳过向量检索，前端在引用片段面板显示「当前 RAG 关闭」标记。
