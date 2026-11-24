package com.demo.rag.tool;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.demo.rag.entity.DocChunk;
import com.demo.rag.repository.DocChunkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Agent 可调用的工具集合
 * 由大模型根据用户问题自主决定是否调用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagTools {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DocChunkMapper docChunkMapper;

    /**
     * 获取当前服务时间
     */
    @Tool(description = "获取当前服务器的时间，返回格式 yyyy-MM-dd HH:mm:ss。当用户询问当前时间、今天日期等问题时调用")
    public String getCurrentTime() {
        String time = LocalDateTime.now().format(TIME_FORMATTER);
        log.info("[工具调用] getCurrentTime, 返回={}", time);
        return time;
    }

    /**
     * 根据文档ID统计分片总数
     */
    @Tool(description = "根据文档ID查询该文档的分片（chunk）总数。当用户询问某个文档包含多少个分片时调用")
    public long countChunksByDocId(
            @ToolParam(description = "文档ID") String docId) {
        Long count = docChunkMapper.selectCount(Wrappers.<DocChunk>lambdaQuery()
                .eq(DocChunk::getDocId, docId));
        long total = count == null ? 0L : count;
        log.info("[工具调用] countChunksByDocId, docId={}, 分片数={}", docId, total);
        return total;
    }
}
