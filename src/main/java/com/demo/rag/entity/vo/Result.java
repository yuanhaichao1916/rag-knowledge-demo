package com.demo.rag.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一接口返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一接口返回结果")
public class Result<T> {

    @Schema(description = "状态码，200 表示成功")
    private Integer code;

    @Schema(description = "提示信息")
    private String msg;

    @Schema(description = "业务数据")
    private T data;

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> ok(T data) {
        return Result.<T>builder()
                .code(200)
                .msg("success")
                .data(data)
                .build();
    }

    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /**
     * 失败返回
     */
    public static <T> Result<T> fail(Integer code, String msg) {
        return Result.<T>builder()
                .code(code)
                .msg(msg)
                .build();
    }
}
