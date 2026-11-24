遵循项目基线 prompt-base.md。
本轮任务：RAG问答模块
1. ChatRagService 接口 + ChatRagServiceImpl
2. ChatController
功能：
1. 普通RAG问答接口
2. SSE流式RAG问答接口
3. 开关 enableRAG，关闭时直接裸问LLM
4. 向量检索topK，拼接上下文，读取resources/prompts/rag-system-prompt.txt
5. 检索不到片段时，返回知识库无信息，禁止编造
6. 返回内容携带source来源片段、相似度分数
7. SSE流式输出遵守text/event-stream协议
8. 输出文件标注文件名
