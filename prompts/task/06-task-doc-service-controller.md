遵循项目基线 prompt-base.md。
本轮任务：文档入库模块
1. DocService 接口 + DocServiceImpl
2. DocController
功能：
1. 上传文本入库：分块、embedding、批量写入pgvector
2. 根据doc_id查询所有分片
3. 根据doc_id删除文档和分片
要求：
1. 调用EmbeddingModel生成向量
2. 批量入库，异常捕获
3. swagger注解
4. 输出文件标注文件名
