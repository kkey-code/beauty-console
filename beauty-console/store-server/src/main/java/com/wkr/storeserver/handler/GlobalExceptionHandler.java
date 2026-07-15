package com.wkr.storeserver.handler;

import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storecommon.exception.IllegalStatusTransitionException;
import com.wkr.storecommon.exception.RateLimitExceededException;
import com.wkr.storecommon.exception.ServiceException;
import com.wkr.storecommon.exception.SystemException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 全局异常处理器，将业务、参数和系统异常转换为统一接口响应并记录日志。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Result<?>> handleRateLimitExceededException(
            RateLimitExceededException e,
            HttpServletRequest request) {
        String message = defaultMessage(e.getMessage(), "请求太频繁，请稍后再试");
        log.warn("Rate limit exceeded: method={}, uri={}, msg={}",
                request.getMethod(), request.getRequestURI(), message);
        return errorResponse(HttpStatus.TOO_MANY_REQUESTS, message);
    }

    @ExceptionHandler({
            BusinessException.class,
            IllegalStatusTransitionException.class,
            ServiceException.class
    })
    public ResponseEntity<Result<?>> handleBusinessException(RuntimeException e, HttpServletRequest request) {
        String message = defaultMessage(e.getMessage(), "业务处理失败");
        log.warn("Business exception: method={}, uri={}, msg={}",
                request.getMethod(), request.getRequestURI(), message);
        return errorResponse(HttpStatus.CONFLICT, message);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Result<?>> handleSystemException(SystemException e, HttpServletRequest request) {
        String message = defaultMessage(e.getMessage(), "系统错误");
        log.error("System exception: method={}, uri={}, query={}",
                request.getMethod(), request.getRequestURI(), request.getQueryString(), e);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<?>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {
        String message = firstFieldErrorMessage(e.getBindingResult().getFieldErrors());
        log.warn("Request body validation failed: method={}, uri={}, msg={}",
                request.getMethod(), request.getRequestURI(), message);
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<?>> handleBindException(BindException e, HttpServletRequest request) {
        String message = firstFieldErrorMessage(e.getBindingResult().getFieldErrors());
        log.warn("Request parameter validation failed: method={}, uri={}, msg={}",
                request.getMethod(), request.getRequestURI(), message);
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<?>> handleConstraintViolationException(
            ConstraintViolationException e,
            HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("参数错误");
        log.warn("Constraint validation failed: method={}, uri={}, msg={}",
                request.getMethod(), request.getRequestURI(), message);
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Result<?>> handleHandlerMethodValidationException(
            HandlerMethodValidationException e,
            HttpServletRequest request) {
        String message = e.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse("参数错误");
        log.warn("Method parameter validation failed: method={}, uri={}, msg={}",
                request.getMethod(), request.getRequestURI(), message);
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<?>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e,
            HttpServletRequest request) {
        String message = "缺少必填参数: " + e.getParameterName();
        log.warn("Missing request parameter: method={}, uri={}, msg={}",
                request.getMethod(), request.getRequestURI(), message);
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Result<?>> handleMissingRequestHeaderException(
            MissingRequestHeaderException e,
            HttpServletRequest request) {
        String message = "缺少必填请求头: " + e.getHeaderName();
        log.warn("Missing request header: method={}, uri={}, msg={}",
                request.getMethod(), request.getRequestURI(), message);
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<?>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {
        String message = "参数类型错误: " + e.getName();
        log.warn("Request parameter type mismatch: method={}, uri={}, msg={}, value={}",
                request.getMethod(), request.getRequestURI(), message, e.getValue());
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<?>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e,
            HttpServletRequest request) {
        log.warn("Request body unreadable: method={}, uri={}, msg={}",
                request.getMethod(), request.getRequestURI(), e.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, "请求参数格式错误");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e, HttpServletRequest request) {
        log.error("Unexpected exception: method={}, uri={}, query={}",
                request.getMethod(), request.getRequestURI(), request.getQueryString(), e);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "系统错误");
    }

    private String firstFieldErrorMessage(Iterable<FieldError> fieldErrors) {
        for (FieldError fieldError : fieldErrors) {
            String message = fieldError.getDefaultMessage();
            if (message != null && !message.isBlank()) {
                return message;
            }
        }

        String fields = StreamSupport.stream(fieldErrors.spliterator(), false)
                .map(FieldError::getField)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
        return fields.isBlank() ? "参数错误" : "参数错误: " + fields;
    }

    private String defaultMessage(String message, String fallback) {
        return message == null || message.isBlank() ? fallback : message;
    }

    private ResponseEntity<Result<?>> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Result.error(status.value(), message));
    }
}
