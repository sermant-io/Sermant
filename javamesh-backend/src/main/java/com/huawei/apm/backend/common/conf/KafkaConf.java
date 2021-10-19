/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.backend.common.conf;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * kafka的配置类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
@Getter
@Setter
@Component
@Configuration
public class KafkaConf {
    // kafka地址
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapServers;

    // common主题名
    @Value("${kafka.heartbeat.topic}")
    private String topicHeartBeat;
}
