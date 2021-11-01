/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据共享配置类
 *
 * @author zhouss
 * @since 2021-10-19
 */
@ConfigurationProperties(prefix = "route.server.share")
@Component
@Getter
@Setter
public class RouteShareProperties {

    private final RedisShareConfiguration redis = new RedisShareConfiguration();

    @Getter
    @Setter
    public static class RedisShareConfiguration {
        /**
         * 最长执行时间
         */
        private final long lockMaxTimeMs = 3000L;

        /**
         * 尝试锁时间
         */
        private final long tryLockIntervalMs = 1000L;

        /**
         * 当前数据最大尝试次数
         */
        private final int maxTryLockCount = 5;
    }
}
