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
