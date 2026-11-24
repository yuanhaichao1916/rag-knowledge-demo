# CHANGELOG - rag-knowledge-demo
记录格式：【序号】｜日期｜任务主题｜改动内容｜决策说明｜坑点&备注｜关联prompt-task文件

【00】｜2026-11-24｜初始化基线文档
改动：新增 prompt-base.md 基线、changelog.md 变更日志
决策：建立提示词管理规范，区分【开发提示词】和【业务LLM prompt】，代码+提示词一起纳入Git管理
坑点：区分两个prompts目录：
  1. 项目根目录 prompts/ ：指挥AI写代码的提示词（开发提示词）
  2. resources/prompts/ ：程序运行传给大模型的system prompt
关联：无

【01】｜2026-11-24｜初始化pom.xml
改动：新建 pom.xml；JDK21(release 21)、Spring Boot 3.3.4、Spring AI 1.0.0-M6、MyBatis-Plus 3.5.5、postgresql、springdoc 2.6.0、lombok；配置 spring milestone 仓库、maven-compiler-plugin、spring-boot-maven-plugin
决策：Spring AI 由 1.0.0-M1 升级到 1.0.0-M6（@Tool 注解自 M6 起提供，M1 仅有 FunctionCallback）
坑点：
  1. M6 的 BOM 仍管理旧名 spring-ai-openai-spring-boot-starter（starter-model-* 改名发生在 M7），写新名会报 version missing
  2. JDK 23+ 默认不再隐式运行注解处理器，需在 maven-compiler-plugin 显式配置 annotationProcessorPaths，并用 Lombok 1.18.48（1.18.34 不支持 JDK 25）
关联：prompts/task/01-task-pom.md

【02】｜2026-11-24｜启动类 + 基础配置文件
改动：新增 RagApplication（@MapperScan com.demo.rag.repository）、application.yml（数据源/Hikari、spring.ai.openai 兼容地址与模型、mybatis-plus、actuator、springdoc、rag 业务参数）
决策：所有敏感项与可变项走环境变量占位；API Key 不落盘
坑点：RAG 参数（chunk-size/overlap/top-k）通过 @Value 注入，yml 键名必须与 Java 中一致
关联：prompts/task/02-task-app-config.md

【03】｜2026-11-24｜数据库初始化SQL
改动：新增 db/init.sql：vector 扩展、doc_chunk 表、embedding vector(1536)、HNSW 余弦索引
决策：HNSW + vector_cosine_ops，检索时用 <=> 保持一致
坑点：需先安装 pgvector 扩展；CREATE EXTENSION 需要超级用户权限
关联：prompts/task/03-task-init-sql.md

【04】｜2026-11-24｜实体、DTO、VO、Mapper
改动：新增 DocChunk、DocUploadDTO、ChatQueryDTO、ChatRespVO、SourceVO、统一返回结构、DocChunkMapper
决策：embedding 用 String 承载 pgvector 文本形式，避免引入 pgvector-java
坑点：写入需 cast(#{embedding} as vector)，否则 PG 报类型不匹配
关联：prompts/task/04-task-entity-mapper.md

【05】｜2026-11-24｜分块工具 ChunkUtil
改动：新增固定长度分块工具，支持 overlap、空文本/空白串返回空集合
决策：非语义切分，实现简单、可预测
坑点：overlap 必须小于 maxChunkSize，否则死循环
关联：prompts/task/05-task-chunk-util.md

【06】｜2026-11-24｜文档入库DocService、DocController
改动：新增 DocService/DocServiceImpl/DocController；上传→分块→批量向量化→逐条入库；查询分片（排除 embedding）、按 docId 删除
决策：@Transactional 保证整篇文档入库原子性；异常包装 RuntimeException 交给全局处理
坑点：Mapper 需自定义 @Insert 完成 vector/jsonb 转换；查询时不返回 embedding 避免响应体过大
关联：prompts/task/06-task-doc-service-controller.md

【07】｜2026-11-24｜RAG问答服务、ChatController（普通+SSE流式）
改动：新增 ChatRagService/ChatRagServiceImpl/ChatController、rag-system-prompt.txt、VectorUtil；普通问答返回 answer+sources；SSE 事件 sources/message/done/error；rag.enable-rag 开关；无检索结果固定回复【知识库未收录相关信息】
决策：System Prompt 放 resources/prompts 并在启动时加载；流式用 SseEmitter + chatModel.stream；GET /api/chat/stream 便于 EventSource 调试
坑点：M6 起底层模型接口为 ChatModel（旧 ChatClient 已重命名），消息取文本用 getText()
关联：prompts/task/07-task-chat-rag-controller.md

【08】｜2026-11-24｜Agent工具类 RagTools、AgentController
改动：新增 RagTools（getCurrentTime、countChunksByDocId，@Tool/@ToolParam，调用打日志）、AgentController（ChatClient.tools 注册，LLM 自主决定调用）
决策：使用 M6 的 @Tool 工具 API；不强制调用
坑点：@Tool 在 M1 不存在，必须 M6+；参数名建议用 @ToolParam 显式声明
关联：prompts/task/08-task-agent-tool.md

【09】｜2026-11-24｜全局异常、统一返回封装
改动：新增 Result（code/msg/data）替换 ResultVO；新增 GlobalExceptionConfig 处理参数校验、请求参数、业务异常、DataAccessException、AI 调用异常与兜底异常
决策：SSE 场景通过 response.isCommitted() 跳过 JSON 写出，避免破坏事件流
坑点：项目未引入 validation starter，@Valid 分支暂不会触发；后续如需校验要加 spring-boot-starter-validation
关联：prompts/task/09-task-global-exception.md

【10】｜2026-11-24｜Dockerfile + README.md
改动：新增 Dockerfile（eclipse-temurin:21-jdk，复制 jar，JAVA_OPTS 启动）、README（简介、mermaid 链路图、环境准备、pgvector 启动、编译启动、Swagger、测试用例、局限与优化）
决策：镜像只做运行态，构建在宿主完成 mvn package
坑点：Dockerfile 内 jar 名硬编码为 rag-knowledge-demo-1.0.0.jar，改版本号需同步修改
关联：prompts/task/10-task-docker-readme.md

【11】｜2026-11-24｜编译与启动验证修复
改动：pom 调整：Spring AI 1.0.0-M6 + spring-ai-openai-spring-boot-starter；MyBatis-Plus 改用 mybatis-plus-spring-boot3-starter:3.5.5；Lombok 1.18.48 + annotationProcessorPaths
决策：保持基线版本号（MP 3.5.5、Boot 3.3.4），仅切换 Spring Boot 3 专用 starter 以兼容 Spring 6.1
坑点：
  1. 旧 mybatis-plus-boot-starter:3.5.5 在 Boot 3.3 启动报 Invalid value type for attribute 'factoryBeanObjectType'（mybatis-spring 2.x）
  2. 本机 JDK 25 编译需 Lombok 1.18.40+
验证：mvn compile / package 成功；jar 启动成功（端口 18080）；/v3/api-docs 返回 200 且含 6 个接口；数据库未启动时接口返回 {"code":500,"msg":"数据库操作异常","data":null}
关联：prompts/task/01-task-pom.md
