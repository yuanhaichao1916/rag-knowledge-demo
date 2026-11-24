package com.demo.rag.service;

import com.demo.rag.entity.dto.ChatQueryDTO;
import com.demo.rag.entity.vo.ChatRespVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * RAG 问答服务
 */
public interface ChatRagService {

    /**
     * 普通问答
     *
     * @param dto 问答请求
     * @return 回答及来源片段
     */
    ChatRespVO chat(ChatQueryDTO dto);

    /**
     * SSE 流式问答
     *
     * @param dto 问答请求
     * @return SSE 事件流
     */
    SseEmitter chatStream(ChatQueryDTO dto);
}
