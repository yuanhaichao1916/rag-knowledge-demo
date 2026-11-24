package com.demo.rag.config;

import com.demo.rag.entity.vo.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理器：统一 code、msg、data 返回结构
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionConfig {

    /**
     * 参数校验异常（@Valid 校验失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("参数校验失败");
        log.warn("参数校验异常: {}", msg);
        return Result.fail(400, msg);
    }

    /**
     * 请求参数异常：缺少参数、类型不匹配、请求体无法解析
     */
    @ExceptionHandler({MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public Result<Void> handleRequestException(Exception e) {
        log.warn("请求参数异常: {}", e.getMessage());
        return Result.fail(400, "请求参数不合法：" + e.getMessage());
    }

    /**
     * 业务参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("业务参数异常: {}", e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /**
     * 业务状态异常（如文档不存在）
     */
    @ExceptionHandler(IllegalStateException.class)
    public Result<Void> handleIllegalStateException(IllegalStateException e) {
        log.warn("业务状态异常: {}", e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /**
     * 数据库异常
     */
    @ExceptionHandler(DataAccessException.class)
    public Result<Void> handleDataAccessException(DataAccessException e) {
        log.error("数据库操作异常", e);
        return Result.fail(500, "数据库操作异常");
    }

    /**
     * 大模型调用异常
     */
    @ExceptionHandler({NonTransientAiException.class, TransientAiException.class})
    public Result<Void> handleAiException(Exception e) {
        log.error("大模型调用异常", e);
        return Result.fail(502, "大模型调用异常：" + e.getMessage());
    }

    /**
     * 兜底异常
     * SSE 流式响应已提交时不能再写 JSON，直接静默结束，避免破坏流式输出
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletResponse response) {
        if (response.isCommitted()) {
            log.warn("响应已提交，跳过统一异常返回: {}", e.getMessage());
            return null;
        }
        log.error("系统异常", e);
        return Result.fail(500, "系统异常：" + e.getMessage());
    }
}
