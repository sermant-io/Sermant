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

package com.huaweicloud.sermant.implement;

import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 日志初始化的实现类
 *
 * @author luanwenfei
 * @since 2022-06-20
 */
public class LoggerFactoryImpl {
    private LoggerFactoryImpl() {
    }

    /**
     * init
     *
     * @return logger logger for sermant
     */
    public static Logger init() {
        Logger logger = java.util.logging.Logger.getLogger("sermant");
        logger.addHandler(new SLF4JBridgeHandler());
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        return logger;
    }
}