/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        return TopicBuilder.name("topic-open-jdk-jvm-monitor")
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
