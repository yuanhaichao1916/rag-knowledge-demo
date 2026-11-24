遵循项目基线 prompt-base.md。
本轮任务：生成 db/init.sql
要求：
1. 先开启vector扩展 create extension if not exists vector;
2. 创建 doc_chunk 表，字段：id bigserial primary key, doc_id varchar(64), title varchar(256), content text, embedding vector(1536), meta jsonb, create_time timestamp default now()
3. 创建hnsw向量索引 idx_doc_chunk_embedding
4. 附带简单注释，只输出sql内容
