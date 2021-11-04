/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.exception;

import com.huawei.route.common.Result;
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
    /**
     * 处理标签组自定义异常
     *
     * @param exception 自定义异常
     * @return Result
     */
    @ResponseBody
    @ExceptionHandler(value = CustomGenericException.class)
    public Result<Object> labelErrorHandler(CustomGenericException exception) {
        if (exception != null) {
            return Result.ofFail(exception.getCode(), exception.getErrMsg());
        }

        return Result.builder().build();
    }

    /**
     * 参数异常处理
     *
     * @param notValidException 参数异常
     * @return Result
     */
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result labelErrorHandler(MethodArgumentNotValidException notValidException) {
        return Result.ofFail(ERROR_CODE_ONE, notValidException.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * 未知异常处理
     *
     * @param exception 异常
     * @return Result
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Result labelErrorHandler(Exception exception) {
        if (exception instanceof HttpMessageNotReadableException) {
            return Result.ofFail(ERROR_CODE_ONE, "参数错误");
        }
        return Result.ofFail(ERROR_CODE_ONE, exception.getMessage());
    }

    /**
     * 绑定异常处理
     *
     * @param bindException 绑定异常
     * @return Result
     */
    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public Result labelErrorHandler(BindException bindException) {
        return Result.ofFail(ERROR_CODE, bindException.getBindingResult().getFieldError().getDefaultMessage());
    }

    /**
     * 校验异常处理
     *
     * @param violationException 校验异常
     * @return Result
     */
    @ResponseBody
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result labelErrorHandler(ConstraintViolationException violationException) {
        return Result.ofFail(ERROR_CODE, violationException.getConstraintViolations().iterator().next().getMessage());
    }
}
