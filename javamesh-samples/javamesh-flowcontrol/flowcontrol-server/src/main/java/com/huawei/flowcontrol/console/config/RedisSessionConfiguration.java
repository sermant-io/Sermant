/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;

import javax.annotation.PostConstruct;

/**
 * 会话超时配置
 *
 * @author zhouss
 * @since 2021-04-23
 **/
@Configuration
public class RedisSessionConfiguration extends RedisHttpSessionConfiguration {

    @Value("${spring.session.timeout:60}")
    private int sessionTimeout;

    @PostConstruct
    @Override
    public void init() {
        super.init();
        super.setMaxInactiveIntervalInSeconds(sessionTimeout);
    }
}
