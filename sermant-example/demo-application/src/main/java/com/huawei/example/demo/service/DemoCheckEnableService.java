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

package com.huawei.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于测试插件内部增强开关是否生效的被拦截点
 *
 * @author lilai
 * @version 1.0.0
 * @since 2022-08-13
 */
public class DemoCheckEnableService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoCheckEnableService.class);

    /**
     * 被拦截的构造函数
     */
    private DemoCheckEnableService() {
        LOGGER.info("DemoCheckEnableService: constructor");
    }

    /**
     * 被拦截的示例方法
     */
    public static void exampleFunc() {
        LOGGER.info("DemoCheckEnableService: exampleFunc");
    }
}
