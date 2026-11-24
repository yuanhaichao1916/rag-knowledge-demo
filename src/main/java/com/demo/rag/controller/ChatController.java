package com.demo.rag.controller;

import com.demo.rag.entity.dto.ChatQueryDTO;
import com.demo.rag.entity.vo.ChatRespVO;
import com.demo.rag.entity.vo.Result;
import com.demo.rag.service.ChatRagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * RAG 问答接口
 */
@Tag(name = "RAG 问答", description = "普通问答与 SSE 流式问答")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRagService chatRagService;

    @Operation(summary = "普通 RAG 问答", description = "向量检索 TopK 片段拼接上下文后调用大模型，返回回答与来源片段")
    @PostMapping
    public Result<ChatRespVO> chat(@RequestBody ChatQueryDTO dto) {
        return Result.ok(chatRagService.chat(dto));
    }

    @Operation(summary = "SSE 流式 RAG 问答", description = "事件类型：sources（来源片段）、message（增量文本）、done（结束）、error（异常）")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @Parameter(description = "用户问题", required = true) @RequestParam String question,
            @Parameter(description = "检索片段数量，不传则使用配置中的 topK") @RequestParam(required = false) Integer topK) {
        ChatQueryDTO dto = ChatQueryDTO.builder()
                .question(question)
                .topK(topK)
                .build();
        return chatRagService.chatStream(dto);
    }
}
