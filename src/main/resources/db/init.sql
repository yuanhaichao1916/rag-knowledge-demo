-- ============================================================
-- RAG 知识库初始化脚本（PostgreSQL 15+ / pgvector）
-- ============================================================

-- 1. 开启 pgvector 向量扩展
create extension if not exists vector;

-- 2. 文档分片表
create table if not exists doc_chunk (
    id          bigserial primary key,
    doc_id      varchar(64)  not null,
    title       varchar(256),
    content     text         not null,
    embedding   vector(1536),
    meta        jsonb,
    create_time timestamp default now()
);

comment on table doc_chunk is '文档分片表';
comment on column doc_chunk.doc_id is '文档ID';
comment on column doc_chunk.title is '文档标题';
comment on column doc_chunk.content is '分片文本内容';
comment on column doc_chunk.embedding is '文本向量（1536维）';
comment on column doc_chunk.meta is '扩展元数据（JSON）';
comment on column doc_chunk.create_time is '创建时间';

-- 3. HNSW 向量索引（余弦距离）
create index if not exists idx_doc_chunk_embedding
    on doc_chunk using hnsw (embedding vector_cosine_ops);
