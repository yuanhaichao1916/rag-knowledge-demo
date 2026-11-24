package com.demo.rag.service.impl;

import com.demo.rag.entity.dto.ChatQueryDTO;
import com.demo.rag.entity.vo.ChatRespVO;
import com.demo.rag.entity.vo.SourceVO;
import com.demo.rag.repository.DocChunkMapper;
import com.demo.rag.service.ChatRagService;
import com.demo.rag.util.VectorUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 问答服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRagServiceImpl implements ChatRagService {

    /** 知识库无相关内容时的固定回复 */
    private static final String NO_KNOWLEDGE_ANSWER = "【知识库未收录相关信息】";

    /** SSE 超时时间（毫秒） */
    private static final long SSE_TIMEOUT = 120_000L;

    private final ChatModel chatModel;

    private final EmbeddingModel embeddingModel;

    private final DocChunkMapper docChunkMapper;

    /** 默认检索片段数量 */
    @Value("${rag.top-k}")
    private int defaultTopK;

    /** RAG 开关：false 时直接裸问大模型 */
    @Value("${rag.enable-rag:true}")
    private boolean enableRag;

    /** System Prompt 文件 */
    @Value("classpath:prompts/rag-system-prompt.txt")
    private Resource systemPromptResource;

    /** System Prompt 内容 */
    private String systemPrompt;

    @PostConstruct
    public void init() throws IOException {
        // 启动时读取 System Prompt，避免硬编码在 Java 代码中
        this.systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public ChatRespVO chat(ChatQueryDTO dto) {
        String question = requireQuestion(dto);
        int topK = resolveTopK(dto);

        List<SourceVO> sources = Collections.emptyList();
        Prompt prompt;
        if (enableRag) {
            // 1. 向量检索
            sources = retrieve(question, topK);
            // 2. 检索不到片段，直接返回固定提示，禁止模型编造
            if (sources.isEmpty()) {
                return ChatRespVO.builder()
                        .answer(NO_KNOWLEDGE_ANSWER)
                        .sources(Collections.emptyList())
                        .build();
            }
            // 3. 拼接上下文构建 Prompt
            prompt = buildRagPrompt(question, sources);
        } else {
            // 关闭 RAG，直接裸问大模型
            prompt = new Prompt(question);
        }

        ChatResponse response = chatModel.call(prompt);
        return ChatRespVO.builder()
                .answer(extractContent(response))
                .sources(sources)
                .build();
    }

    @Override
    public SseEmitter chatStream(ChatQueryDTO dto) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        String question;
        int topK;
        try {
            question = requireQuestion(dto);
            topK = resolveTopK(dto);
        } catch (Exception e) {
            safeSend(emitter, SseEmitter.event().name("error").data(e.getMessage()));
            emitter.complete();
            return emitter;
        }

        Prompt prompt;
        if (enableRag) {
            List<SourceVO> sources = retrieve(question, topK);
            // 先推送来源片段
            safeSend(emitter, SseEmitter.event().name("sources").data(sources, MediaType.APPLICATION_JSON));
            if (sources.isEmpty()) {
                // 无检索结果，直接返回固定提示并结束
                safeSend(emitter, SseEmitter.event().name("message").data(NO_KNOWLEDGE_ANSWER));
                safeSend(emitter, SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
                return emitter;
            }
            prompt = buildRagPrompt(question, sources);
        } else {
            prompt = new Prompt(question);
        }

        // 订阅流式响应，按 SSE 协议推送增量内容
        Flux<ChatResponse> flux = chatModel.stream(prompt);
        flux.subscribeOn(Schedulers.boundedElastic()).subscribe(
                response -> {
                    String delta = extractContent(response);
                    if (delta != null && !delta.isEmpty()) {
                        safeSend(emitter, SseEmitter.event().name("message").data(delta));
                    }
                },
                error -> {
                    log.error("SSE 流式问答异常", error);
                    String msg = error.getMessage() == null ? "流式问答异常" : error.getMessage();
                    safeSend(emitter, SseEmitter.event().name("error").data(msg));
                    emitter.complete();
                },
                () -> {
                    safeSend(emitter, SseEmitter.event().name("done").data("[DONE]"));
                    emitter.complete();
                });
        return emitter;
    }

    /**
     * 向量检索 TopK 片段
     */
    private List<SourceVO> retrieve(String question, int topK) {
        float[] vector = embeddingModel.embed(question);
        return docChunkMapper.searchSimilar(VectorUtil.toPgVector(vector), topK);
    }

    /**
     * 构建 RAG Prompt：System Prompt + 知识片段 + 用户问题
     */
    private Prompt buildRagPrompt(String question, List<SourceVO> sources) {
        String context = sources.stream()
                .map(source -> "《" + (source.getTitle() == null ? "未命名文档" : source.getTitle()) + "》\n" + source.getContent())
                .collect(Collectors.joining("\n\n---\n\n"));
        String userContent = "【知识片段】\n" + context + "\n\n【用户问题】\n" + question;
        List<Message> messages = List.of(new SystemMessage(systemPrompt), new UserMessage(userContent));
        return new Prompt(messages);
    }

    /**
     * 提取模型返回文本
     */
    private String extractContent(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        String content = response.getResult().getOutput().getText();
        return content == null ? "" : content;
    }

    /**
     * 校验问题不能为空
     */
    private String requireQuestion(ChatQueryDTO dto) {
        if (dto == null || dto.getQuestion() == null || dto.getQuestion().isBlank()) {
            throw new IllegalArgumentException("问题不能为空");
        }
        return dto.getQuestion();
    }

    /**
     * 解析检索数量，非法时使用默认配置
     */
    private int resolveTopK(ChatQueryDTO dto) {
        Integer topK = dto.getTopK();
        if (topK == null || topK <= 0) {
            return defaultTopK;
        }
        return topK;
    }

    /**
     * 安全发送 SSE 事件，客户端断开时静默结束
     */
    private void safeSend(SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException | IllegalStateException e) {
            log.warn("SSE 发送失败，客户端可能已断开: {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }
}
