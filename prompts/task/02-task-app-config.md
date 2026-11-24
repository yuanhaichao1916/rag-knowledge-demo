遵循项目基线 prompt-base.md。
本轮任务：
1. 生成启动类 RagApplication.java
2. 生成 application.yml
要求：
1. application.yml包含：服务端口、数据库pgvector配置、通义openai兼容配置（base-url、api-key、chat模型、embedding模型）
2. 配置mybatis-plus、actuator全部端点暴露、swagger
3. 可配置参数：chunkSize、overlap、topK
4. 代码增加注释，yml不要硬编码敏感key，预留占位
5. 依次输出两个文件，标明文件名
