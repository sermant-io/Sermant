/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.recordconsole;

import com.huawei.recordconsole.consumer.RecordConsoleConsumer;
import com.huawei.recordconsole.elasticsearch.ElasticSearchIndexCreator;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * Springboot主入口
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-02-19
 */

@SpringBootApplication
public class RecordConsoleApplication implements ApplicationRunner {
    /**
     * 自动注入自定义的kafka消费者consumer对象
     */
    @Autowired
    RecordConsoleConsumer consumer;
    @Autowired
    private ElasticSearchIndexCreator elasticSearchimpl;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public static void main(String[] args) {
        SpringApplication.run(RecordConsoleApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException, IOException {
        startSubscribeKafka();
    }

    /**
     * 启动kafka consumer
     */
    public void startSubscribeKafka() {
        consumer.start();
    }
}