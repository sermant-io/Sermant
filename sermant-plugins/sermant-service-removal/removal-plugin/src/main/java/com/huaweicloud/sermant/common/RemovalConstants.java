/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.common;

/**
 * 常量类
 *
 * @author zhp
 * @since 2023-04-04
 */
public class RemovalConstants {
    /**
     * Dubbo服务名称KEY
     */
    public static final String APPLICATION_KEY = "application";

    /**
     * 连接符
     */
    public static final String CONNECTOR = ":";

    /**
     * 时间窗口大小
     */
    public static final int WINDOWS_TIME = 1000;

    /**
     * 窗口个数
     */
    public static final int WINDOWS_NUM = 10;

    /**
     * 插件名称
     */
    public static final String PLUGIN_NAME = "serment-service-removal";

    /**
     * 动态配置KEY
     */
    public static final String DYNAMIC_CONFIG_KEY = "sermant.removal.config";

    private RemovalConstants() {
    }
}
