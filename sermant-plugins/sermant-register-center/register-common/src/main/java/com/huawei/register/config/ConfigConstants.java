/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.register.config;

/**
 * 配置常量定义
 *
 * @author zhouss
 * @since 2022-03-02
 */
public class ConfigConstants {
    /**
     * 默认实例拉取间隔 单位秒
     */
    public static final int DEFAULT_PULL_INTERVAL = 15;

    /**
     * 默认心跳失败重试次数
     */
    public static final int DEFAULT_HEARTBEAT_RETRY_TIMES = 3;

    /**
     * 默认心跳发送间隔 单位秒
     */
    public static final int DEFAULT_HEARTBEAT_INTERVAL = 15;

    private ConfigConstants() {
    }
}
