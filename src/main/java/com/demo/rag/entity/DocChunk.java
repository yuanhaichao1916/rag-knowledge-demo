package com.demo.rag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档分片实体，对应 doc_chunk 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("doc_chunk")
public class DocChunk {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文档ID */
    private String docId;

    /** 文档标题 */
    private String title;

    /** 分片文本内容 */
    private String content;

    /** 文本向量，pgvector 字符串形式，例如 "[0.1,0.2,...]" */
    private String embedding;

    /** 扩展元数据（JSON 字符串，对应 jsonb） */
    private String meta;

    /** 创建时间 */
    private LocalDateTime createTime;
}
