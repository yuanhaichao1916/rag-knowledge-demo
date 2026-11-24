package com.demo.rag.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文本分块工具：按固定字符长度切分，支持相邻分块重叠（非语义切分）。
 *
 * <p>单元测试要点：</p>
 * <pre>
 * 1. split(null, 10, 2)          -&gt; 返回空集合
 * 2. split("   ", 10, 2)         -&gt; 返回空集合
 * 3. split("abcdefghij", 4, 1)   -&gt; ["abcd", "defg", "ghij"]
 * 4. split("abc", 10, 2)         -&gt; ["abc"]
 * 5. split("abcdef", 4, 4)       -&gt; 抛出 IllegalArgumentException（overlap 必须小于 maxChunkSize）
 * </pre>
 */
public final class ChunkUtil {

    private ChunkUtil() {
    }

    /**
     * 固定长度分块
     *
     * @param text         原始文本
     * @param maxChunkSize 单个分块最大字符数，必须大于 0
     * @param overlap      相邻分块的重叠字符数，取值范围 [0, maxChunkSize)
     * @return 分块结果；文本为空或空白时返回空集合
     */
    public static List<String> split(String text, int maxChunkSize, int overlap) {
        // 空文本、空白文本直接返回空集合
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        if (maxChunkSize <= 0) {
            throw new IllegalArgumentException("maxChunkSize 必须大于 0");
        }
        if (overlap < 0 || overlap >= maxChunkSize) {
            throw new IllegalArgumentException("overlap 必须大于等于 0 且小于 maxChunkSize");
        }

        List<String> chunks = new ArrayList<>();
        // 每次前进的步长 = 分块长度 - 重叠长度
        int step = maxChunkSize - overlap;
        for (int start = 0; start < text.length(); start += step) {
            int end = Math.min(start + maxChunkSize, text.length());
            String chunk = text.substring(start, end);
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }
            // 已到文本末尾，结束切分
            if (end == text.length()) {
                break;
            }
        }
        return chunks;
    }
}
