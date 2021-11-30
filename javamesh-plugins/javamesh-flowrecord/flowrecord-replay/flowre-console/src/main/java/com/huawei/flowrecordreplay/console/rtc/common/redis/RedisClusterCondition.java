/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.rtc.common.redis;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * redis集群注入条件
 *
 * @author hanpeng
 * @since 2021-04-07
 */
public class RedisClusterCondition implements Condition {
    /**
     * 通过读取配置文件中的值获取注入条件
     *
     * @param context  上下文
     * @param metadata 注解元数据
     * @return 返回是否匹配，true表示匹配 false为不匹配
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return Boolean.parseBoolean(context.getEnvironment().getProperty("spring.redis.isCluster"));
    }
}
