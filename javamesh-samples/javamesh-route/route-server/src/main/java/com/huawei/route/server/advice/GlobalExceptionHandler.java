/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.route.server.advice;

import com.huawei.route.common.Result;
import io.lettuce.core.RedisException;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import redis.clients.jedis.exceptions.JedisException;

/**
 * 功能描述：全局为捕获异常处理类
 *
 * @author z30009938
 * @since 2021-09-21
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 日志工具
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 错误码
     */
    private static final int ERROR_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    @ExceptionHandler(Exception.class)
    public Result<String> handleNotCatchException(Exception exception) {
        LOGGER.error("An error occur which not be caught in system.", exception);
        return Result.ofFail(ERROR_CODE, "操作失败，系统发生一个未知异常！");
    }

    @ExceptionHandler({KafkaException.class, org.springframework.kafka.KafkaException.class})
    public Result<String> handleKafkaException(Exception exception) {
        LOGGER.error("An error occur in system when using kafka.", exception);
        return Result.ofFail(ERROR_CODE, "操作失败，请检查kafka相关组件！");
    }

    @ExceptionHandler({JedisException.class, RedisException.class})
    public Result<String> handleRedisException(Exception exception) {
        LOGGER.error("An error occur in system when using redis.", exception);
        return Result.ofFail(ERROR_CODE, "操作失败，请检查redis相关组件！");
    }

    @ExceptionHandler({
            org.apache.curator.shaded.com.google.common.base.VerifyException.class,
            org.apache.curator.shaded.com.google.common.cache.CacheLoader.InvalidCacheLoadException.class,
            org.apache.curator.shaded.com.google.common.collect.ComputationException.class,
            org.apache.curator.shaded.com.google.common.util.concurrent.UncheckedExecutionException.class,
            org.apache.curator.shaded.com.google.common.util.concurrent.UncheckedTimeoutException.class,
            org.apache.curator.framework.schema.SchemaViolation.class,
            org.apache.curator.framework.recipes.leader.CancelLeadershipException.class
    })
    public Result<String> handleZookeeperException(Exception exception) {
        LOGGER.error("An error occur in system when using zookeeper.", exception);
        return Result.ofFail(ERROR_CODE, "操作失败，请检查Zookeeper相关组件！");
    }
}
