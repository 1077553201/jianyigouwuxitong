package com.aegis.shujukuhouduan.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 * 用于业务逻辑中的错误抛出，如"用户名已存在"、"库存不足"等
 * 会被 GlobalExceptionHandler 捕获并返回统一格式
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码（默认400） */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
