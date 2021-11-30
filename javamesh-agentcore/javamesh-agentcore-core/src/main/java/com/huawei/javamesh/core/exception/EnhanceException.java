package com.huawei.javamesh.core.exception;

/**
 * 增强过程异常类
 */
public class EnhanceException extends RuntimeException {

    private static final long serialVersionUID = -4463670023518646571L;

    public EnhanceException(String message) {
        super(message);
    }

    public EnhanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
