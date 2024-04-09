/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

import com.huaweicloud.integration.controller.ConsumerController;
import com.huaweicloud.integration.controller.FlowController;
import com.huaweicloud.integration.controller.ProviderController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;

/**
 * 启动类
 *
 * @author provenceee
 * @since 2022-04-28
 */
@SpringBootApplication
@ImportResource({"classpath:dubbo/consumer.xml"})
@ComponentScan(excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, value = {ProviderController.class,
    ConsumerController.class, FlowController.class}))
public class ControllerApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerApplication.class);

    /**
     * spring启动类
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        LOGGER.info("====================start=======================");
        SpringApplication.run(ControllerApplication.class);
        LOGGER.info("=====================end========================");
    }
}
