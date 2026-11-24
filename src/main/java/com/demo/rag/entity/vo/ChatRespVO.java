package com.demo.rag.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 问答响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "问答响应")
public class ChatRespVO {

    @Schema(description = "模型回答")
    private String answer;

    @Schema(description = "引用的知识片段列表")
    private java.util.List<SourceVO> sources;
}
