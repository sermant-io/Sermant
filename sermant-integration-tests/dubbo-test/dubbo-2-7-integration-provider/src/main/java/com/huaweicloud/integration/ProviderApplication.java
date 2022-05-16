/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * 启动类
 *
 * @author provenceee
 * @since 2022-04-27
 */
@SpringBootApplication
@ImportResource({"classpath:dubbo/provider.xml"})
public class ProviderApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderApplication.class);

    /**
     * spring启动类
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        LOGGER.info("====================start=======================");
        SpringApplication.run(ProviderApplication.class);
        LOGGER.info("=====================end========================");
    }
}