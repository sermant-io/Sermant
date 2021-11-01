/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.exception;

import com.huawei.route.server.common.Result;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolationException;

import static com.huawei.route.server.labels.constant.LabelConstant.ERROR_CODE;
import static com.huawei.route.server.labels.constant.LabelConstant.ERROR_CODE_ONE;

/**
 * 标签与配置的全局异常处理类
 *
 * @author zhanghu
 * @since 2021-04-20
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LabelExceptionHandler {
    @ResponseBody
    @ExceptionHandler(value = CustomGenericException.class)
    public Result<Object> labelErrorHandler(CustomGenericException e) {
        if (e != null) {
            return Result.ofFail(e.getCode(), e.getErrMsg());
        }

        return Result.builder().build();
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result labelErrorHandler(MethodArgumentNotValidException e) {
        return Result.ofFail(ERROR_CODE_ONE, e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Result labelErrorHandler(Exception exception) {
        if (exception instanceof HttpMessageNotReadableException) {
            return Result.ofFail(ERROR_CODE_ONE, "参数错误");
        }
        return Result.ofFail(ERROR_CODE_ONE, exception.getMessage());

    }

    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public Result labelErrorHandler(BindException e) {
        return Result.ofFail(ERROR_CODE, e.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result labelErrorHandler(ConstraintViolationException e) {
        return Result.ofFail(ERROR_CODE, e.getConstraintViolations().iterator().next().getMessage());
    }
}
