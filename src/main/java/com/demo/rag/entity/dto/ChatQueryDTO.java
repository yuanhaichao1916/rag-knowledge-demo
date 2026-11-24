package com.demo.rag.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 问答请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "问答请求")
public class ChatQueryDTO {

    @Schema(description = "用户问题", example = "Spring AI 如何接入通义千问？")
    private String question;

    @Schema(description = "检索片段数量，不传则使用配置中的 topK", example = "5")
    private Integer topK;
}
