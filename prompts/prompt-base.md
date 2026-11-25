# 项目基线：rag-knowledge-demo
> 本基线为固定约束，所有交给AI生成代码的任务，都必须遵守此基线；除非明确变更基线，否则不允许擅自修改技术栈、引入额外组件。

## 基础信息
- 项目名称：rag-knowledge-demo
- 项目类型：Java后端RAG知识库API服务
- JDK：21
- 构建工具：Maven
- 主框架：SpringBoot 3.3.x
- AI框架：Spring AI 1.0.x
- 数据库：PostgreSQL 15+ + pgvector 向量插件
- 向量维度：1536（适配通义千问 OpenAI 兼容 Embedding）
- 调试方式：OpenAPI / Swagger，**不做前端页面**

## 包结构（严格遵守）
rag-knowledge-demo
├── pom.xml
├── src/main/java/com/demo/rag
│   ├── RagApplication.java
│   ├── config
│   │   ├── SpringAiConfig.java
│   │   └── GlobalExceptionConfig.java
│   ├── controller
│   │   ├── DocController.java
│   │   ├── ChatController.java
│   │   └── AgentController.java
│   ├── service
│   │   ├── DocService.java
│   │   ├── ChatRagService.java
│   │   └── impl
│   ├── repository
│   │   └── DocChunkMapper.java
│   ├── entity
│   │   ├── DocChunk.java
│   │   ├── dto
│   │   │   ├── DocUploadDTO.java
│   │   │   └── ChatQueryDTO.java
│   │   └── vo
│   │       ├── ChatRespVO.java
│   │       └── SourceVO.java
│   ├── util
│   │   ├── ChunkUtil.java
│   │   └── VectorUtil.java
│   └── tool
│       └── RagTools.java
├── src/main/resources
│   ├── application.yml
│   ├── prompts
│   │   └── rag-system-prompt.txt
│   └── db
│       └── init.sql
├── Dockerfile
└── README.md

## 功能约束
1. 文档入库：支持纯文本/Markdown上传；固定长度分块 + overlap重叠，**暂不做语义切分**
2. RAG问答链路：文本分块 → Embedding向量化 → pgvector入库；提问时向量检索TopK片段，拼接上下文给LLM
3. 检索不到相关片段时，禁止大模型编造内容，需要明确告知【知识库未收录相关信息】
4. 支持普通问答 + SSE流式问答两种模式
5. Agent 使用 Spring AI @Tool 注解实现工具调用，LLM自主判断是否调用工具
6. 提供文档删除、根据doc_id查询分片、健康检查接口

## 前端约定
1. 技术栈：Vue 3.4 + Vite 5 + Element Plus 2.7
2. 目录：仓库根目录 `frontend/`，与后端 `src/` 平级
3. 运行方式：开发模式 Vite 独立 dev server（5173 端口），通过 proxy 转发 `/api` 和 `/v3` 到后端 8080
4. 构建产物：`frontend/dist/`，可选部署到 Nginx 或 Spring Boot `static/`
5. 页面范围：问答主界面、文档管理、Agent 工具调试台、接口文档（Swagger UI 内嵌）

## 工程规范
1. 统一全局异常处理器，统一接口返回体结构
2. LLM的System Prompt放到独立txt文件，禁止硬编码在Java代码
3. 模型Key、模型名称、topK、chunk大小、overlap等配置全部写在application.yml，禁止硬编码
4. 引入actuator健康检查端点
5. 接口文档使用 springdoc-openapi（Swagger）
6. 禁止额外引入：Redis、MQ、Milvus、Elasticsearch、消息队列等中间件
7. 代码带中文注释，命名遵循Java规范
8. 输出文件时，每段代码前标注文件名

## 模型信息
- 接入方式：通义千问 OpenAI 兼容接口
- chat模型：qwen-turbo
- embedding模型：text-embedding-v1
- embedding维度：1536
