package com.demo.rag.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档上传请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档上传请求")
public class DocUploadDTO {

    @Schema(description = "文档标题", example = "Spring AI 使用手册")
    private String title;

    @Schema(description = "文档内容（纯文本 / Markdown）", example = "这里是文档正文...")
    private String content;

    @Schema(description = "扩展元数据（JSON 字符串，可选）", example = "{\"source\":\"manual\"}")
    private String meta;
}
