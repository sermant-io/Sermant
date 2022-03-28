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

package com.huawei.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 名称匹配的示例被拦截点
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public class DemoNameService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoNameService.class);

    /**
     * 被拦截的构造函数
     */
    public DemoNameService() {
        LOGGER.info("DemoNameService: constructor");
    }

    /**
     * 被拦截的静态方法
     */
    public static void staticFunc() {
        LOGGER.info("DemoNameService: staticFunc");
    }

    /**
     * 用于测试服务
     */
    public static void serviceFunc() {
        LOGGER.info("DemoNameService: serviceFunc");
    }

    /**
     * 用于测试统一配置
     */
    public static void configFunc() {
        LOGGER.info("DemoNameService: configFunc");
    }

    /**
     * 被拦截的实例方法
     */
    public void memberFunc() {
        LOGGER.info("DemoNameService: memberFunc");
    }

    /**
     * 用于测试字段设置
     */
    public void fieldFunc() {
        LOGGER.info("DemoNameService: fieldFunc");
    }

    /**
     * 用于测试接口
     */
    public void interfaceFunc() {
        LOGGER.info("DemoNameService: interfaceFunc");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
