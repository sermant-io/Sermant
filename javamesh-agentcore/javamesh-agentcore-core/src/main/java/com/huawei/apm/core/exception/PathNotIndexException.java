package com.huawei.apm.core.exception;

public class PathNotIndexException extends RuntimeException {
    public PathNotIndexException() {
        super("Index relative path failed, please check. ");
    }
}
