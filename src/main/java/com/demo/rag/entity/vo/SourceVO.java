package com.demo.rag.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识来源片段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识来源片段")
public class SourceVO {

    @Schema(description = "文档ID")
    private String docId;

    @Schema(description = "文档标题")
    private String title;

    @Schema(description = "片段内容")
    private String content;

    @Schema(description = "相似度得分（越大越相关）")
    private Double score;
}
