package com.demo.rag.util;

/**
 * 向量工具类
 */
public final class VectorUtil {

    private VectorUtil() {
    }

    /**
     * float 数组转换为 pgvector 文本格式，例如 [0.1,0.2]
     *
     * @param vector 向量数组
     * @return pgvector 文本形式
     */
    public static String toPgVector(float[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("向量不能为空");
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector[i]);
        }
        return sb.append(']').toString();
    }
}
