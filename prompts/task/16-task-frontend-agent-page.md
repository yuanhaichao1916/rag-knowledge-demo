遵循项目基线 prompt-base.md 中的「前端约定」。

本轮任务：Agent 工具调试台（frontend/src/views/Agent.vue）

功能：
1. 独立聊天界面（简化版，复用 Chat.vue 组件或内联）
2. 工具栏：
   - 预置问题快捷按钮：「现在几点？」「docId=xxx 有多少个分片？」
   - 自定义问题输入框
3. 调用 POST /api/agent/chat，展示返回内容
4. **注意**：Spring AI 1.0.0-M6 的 Agent 工具调用内部日志后端没暴露给前端，所以无法直接看到"模型在第几轮调用了哪个工具"。
   - 简化方案：在后端 AgentController 内给每条工具调用加 MDC + INFO 日志，前端展示最终回答即可
   - 不做"工具调用轨迹可视化"（后端没给 OpenTelemetry 链路）
5. 样式与 Chat.vue 保持一致，通过 tag 或小图标区分「Agent」 vs 「RAG」会话

**不要**为了展示工具调用轨迹去改后端新增接口，超出任务范围。
