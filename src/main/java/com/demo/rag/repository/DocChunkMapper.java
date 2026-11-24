package com.demo.rag.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.rag.entity.DocChunk;
import com.demo.rag.entity.vo.SourceVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文档分片 Mapper
 */
public interface DocChunkMapper extends BaseMapper<DocChunk> {

    /**
     * 插入单个分片，embedding 显式转换为 pgvector 类型
     *
     * @param chunk 分片实体
     * @return 影响行数
     */
    @Insert("insert into doc_chunk (doc_id, title, content, embedding, meta) "
            + "values (#{docId}, #{title}, #{content}, cast(#{embedding} as vector), cast(#{meta} as jsonb))")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertChunk(DocChunk chunk);

    /**
     * 向量相似度检索 TopK 片段（余弦距离）
     *
     * @param embedding 查询向量，pgvector 文本形式
     * @param topK      返回条数
     * @return 相似片段列表，包含相似度分数
     */
    @Select("select doc_id, title, content, 1 - (embedding <=> cast(#{embedding} as vector)) as score "
            + "from doc_chunk where embedding is not null "
            + "order by embedding <=> cast(#{embedding} as vector) limit #{topK}")
    List<SourceVO> searchSimilar(@Param("embedding") String embedding, @Param("topK") int topK);
}
