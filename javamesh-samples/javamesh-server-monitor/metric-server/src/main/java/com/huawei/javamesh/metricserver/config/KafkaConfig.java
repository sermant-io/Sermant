/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka配置
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic topicOfIbmJvmMonitor() {
        return TopicBuilder.name("topic-ibm-jvm-monitor")
            .build();
    }

    @Bean
    public NewTopic topicOfOracleJvmMonitor() {
        return TopicBuilder.name("topic-oracle-jvm-monitor")
            .build();
    }

    @Bean
    public NewTopic topicOfServerMonitor() {
        return TopicBuilder.name("topic-server-monitor")
            .build();
    }

    @Bean
    public NewTopic topicOfAgentRegistration() {
        return TopicBuilder.name("topic-agent-registration")
            .build();
    }

    @Bean
    public NewTopic topicOfDruidMonitor() {
        return TopicBuilder.name("topic-druid-monitor")
            .build();
    }
}
