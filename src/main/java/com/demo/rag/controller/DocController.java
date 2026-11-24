package com.demo.rag.controller;

import com.demo.rag.entity.DocChunk;
import com.demo.rag.entity.dto.DocUploadDTO;
import com.demo.rag.entity.vo.Result;
import com.demo.rag.service.DocService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文档管理接口
 */
@Tag(name = "文档管理", description = "文档上传入库、分片查询、文档删除")
@RestController
@RequestMapping("/api/doc")
@RequiredArgsConstructor
public class DocController {

    private final DocService docService;

    @Operation(summary = "上传文本并入库", description = "文本分块、向量化后写入 pgvector，返回文档ID")
    @PostMapping("/upload")
    public Result<String> upload(@RequestBody DocUploadDTO dto) {
        return Result.ok(docService.upload(dto));
    }

    @Operation(summary = "根据文档ID查询分片", description = "查询指定文档的全部分片（不返回向量字段）")
    @GetMapping("/{docId}/chunks")
    public Result<List<DocChunk>> listChunks(
            @Parameter(description = "文档ID") @PathVariable String docId) {
        return Result.ok(docService.listByDocId(docId));
    }

    @Operation(summary = "根据文档ID删除文档", description = "删除指定文档及其全部分片，返回删除的分片数量")
    @DeleteMapping("/{docId}")
    public Result<Integer> delete(
            @Parameter(description = "文档ID") @PathVariable String docId) {
        return Result.ok(docService.deleteByDocId(docId));
    }
}
