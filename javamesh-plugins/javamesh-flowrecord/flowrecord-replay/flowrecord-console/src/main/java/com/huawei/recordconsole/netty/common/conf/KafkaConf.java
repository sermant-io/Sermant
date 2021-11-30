/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.recordconsole.netty.common.conf;

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
 * @since 2021-07-12
 */
@Getter
@Setter
@Component
@Configuration
public class KafkaConf {
    // kafka地址
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapServers;

    // record主题名
    @Value("${topic.record}")
    private String topicRecord;
}
