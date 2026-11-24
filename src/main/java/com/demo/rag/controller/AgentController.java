package com.demo.rag.controller;

import com.demo.rag.entity.dto.ChatQueryDTO;
import com.demo.rag.entity.vo.Result;
import com.demo.rag.tool.RagTools;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 工具调用接口
 * 工具由大模型自主决定是否调用
 */
@Slf4j
@Tag(name = "Agent 工具调用", description = "大模型自主判断是否调用工具")
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final ChatClient chatClient;

    private final RagTools ragTools;

    public AgentController(ChatModel chatModel, RagTools ragTools) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.ragTools = ragTools;
    }

    @Operation(summary = "Agent 问答", description = "将 RagTools 注册为可用工具，由大模型自主决定是否调用")
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatQueryDTO dto) {
        if (dto == null || dto.getQuestion() == null || dto.getQuestion().isBlank()) {
            throw new IllegalArgumentException("问题不能为空");
        }
        log.info("Agent 问答请求: {}", dto.getQuestion());

        String answer = chatClient.prompt()
                .user(dto.getQuestion())
                // 注册工具，是否调用由大模型自主判断
                .tools(ragTools)
                .call()
                .content();

        return Result.ok(answer);
    }
}
