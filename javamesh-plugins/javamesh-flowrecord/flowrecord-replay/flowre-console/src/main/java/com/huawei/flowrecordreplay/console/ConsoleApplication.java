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

package com.huawei.flowrecordreplay.console;

import com.huawei.flowrecordreplay.console.rtc.consumer.RtcConsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ConsoleApplication implements ApplicationRunner {
    /**
     * 自动注入自定义的kafka消费者consumer对象
     */
    @Autowired
    RtcConsumer consumer;

    /**
     * 下发录制任务
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ConsoleApplication.class, args);
    }

    /**
     * 启动kafka consumer
     */
    @Override
    public void run(ApplicationArguments args) {
    }
}
