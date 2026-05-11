package com.sh.aishop.common;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理各类异常，返回 Result 格式的友好响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========== 1. 参数校验异常（@Valid 校验失败） ==========
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(FieldError::getDefaultMessage)
            .orElse("参数校验失败");
        return Result.fail(ResultCode.PARAM_VALID_FAIL, message);
    }

    // ========== 2. JSON 反序列化失败（类型不匹配） ==========
    @ExceptionHandler(InvalidFormatException.class)
    public Result<?> handleInvalidFormat(InvalidFormatException ex) {
        String field = ex.getPath().stream()
            .map(ref -> ref.getFieldName())
            .collect(Collectors.joining("."));
        String message = field.isEmpty() ? "请求格式错误" : "字段 [" + field + "] 类型错误";
        return Result.fail(ResultCode.REQUEST_FORMAT_ERROR, message);
    }

    // ========== 3. 请求参数缺少 ==========
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = "缺少必要参数: " + ex.getParameterName();
        return Result.fail(ResultCode.MISSING_PARAM, message);
    }

    // ========== 4. 参数类型转换失败（如路径变量类型错误） ==========
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "参数 " + ex.getName() + " 类型错误";
        return Result.fail(ResultCode.REQUEST_FORMAT_ERROR, message);
    }

    // ========== 5. 兜底处理其他验证异常 ==========
    @ExceptionHandler(HandlerMethodValidationException.class)
    public Result<?> handleHandlerValidation(HandlerMethodValidationException ex) {
        return Result.fail(ResultCode.PARAM_VALID_FAIL, "参数校验失败");
    }

    // ========== 6. 业务异常 ==========
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException ex) {
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    // ========== 7. 兜底处理未知异常 ==========
    @ExceptionHandler(Exception.class)
    public Result<?> handleOther(Exception ex, HttpServletRequest request) {
        // 生产环境不打印堆栈，只记日志
        String method = request.getMethod();
        String uri = request.getRequestURI();
        System.err.println("[" + method + " " + uri + "] Unhandled exception: " + ex.getClass().getName());
        return Result.fail(ResultCode.SERVER_ERROR, "系统繁忙，请稍后重试");
    }
}