package com.demo.rag.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.demo.rag.entity.DocChunk;
import com.demo.rag.entity.dto.DocUploadDTO;
import com.demo.rag.repository.DocChunkMapper;
import com.demo.rag.service.DocService;
import com.demo.rag.util.ChunkUtil;
import com.demo.rag.util.VectorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 文档管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocServiceImpl implements DocService {

    private final DocChunkMapper docChunkMapper;

    private final EmbeddingModel embeddingModel;

    /** 分块长度 */
    @Value("${rag.chunk-size}")
    private int chunkSize;

    /** 分块重叠长度 */
    @Value("${rag.overlap}")
    private int overlap;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String upload(DocUploadDTO dto) {
        if (dto == null || dto.getContent() == null || dto.getContent().isBlank()) {
            throw new IllegalArgumentException("文档内容不能为空");
        }

        // 1. 固定长度分块
        List<String> chunks = ChunkUtil.split(dto.getContent(), chunkSize, overlap);
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("文档内容分块结果为空");
        }

        String docId = UUID.randomUUID().toString().replace("-", "");
        try {
            // 2. 批量生成向量
            List<float[]> vectors = embeddingModel.embed(chunks);
            if (vectors.size() != chunks.size()) {
                throw new IllegalStateException("向量数量与分块数量不一致");
            }

            // 3. 逐条写入 pgvector
            for (int i = 0; i < chunks.size(); i++) {
                DocChunk chunk = DocChunk.builder()
                        .docId(docId)
                        .title(dto.getTitle())
                        .content(chunks.get(i))
                        .embedding(VectorUtil.toPgVector(vectors.get(i)))
                        .meta(dto.getMeta())
                        .build();
                docChunkMapper.insertChunk(chunk);
            }
        } catch (Exception e) {
            log.error("文档入库失败, docId={}", docId, e);
            throw new RuntimeException("文档入库失败：" + e.getMessage(), e);
        }

        log.info("文档入库成功, docId={}, 分块数={}", docId, chunks.size());
        return docId;
    }

    @Override
    public List<DocChunk> listByDocId(String docId) {
        return docChunkMapper.selectList(Wrappers.<DocChunk>lambdaQuery()
                .eq(DocChunk::getDocId, docId)
                // 不返回向量字段，避免响应体过大
                .select(DocChunk.class, field -> !"embedding".equals(field.getColumn()))
                .orderByAsc(DocChunk::getId));
    }

    @Override
    public int deleteByDocId(String docId) {
        int deleted = docChunkMapper.delete(Wrappers.<DocChunk>lambdaQuery()
                .eq(DocChunk::getDocId, docId));
        if (deleted == 0) {
            throw new IllegalArgumentException("文档不存在：" + docId);
        }
        log.info("文档删除成功, docId={}, 分片数={}", docId, deleted);
        return deleted;
    }
}
