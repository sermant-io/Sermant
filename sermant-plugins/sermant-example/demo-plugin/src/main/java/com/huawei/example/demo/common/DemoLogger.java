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

package com.huawei.example.demo.common;

import com.huawei.sermant.core.common.LoggerFactory;

import java.util.logging.Logger;

/**
 * 示例插件的日志类
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-26
 */
public class DemoLogger {
    private static final Logger logger = LoggerFactory.getLogger();

    private DemoLogger() {
    }

    /**
     * 输出日志到控制台，采用ERROR级别，否则会被屏蔽
     *
     * @param message 日志信息
     */
    public static void println(String message) {
        logger.severe(message);
    }
}
