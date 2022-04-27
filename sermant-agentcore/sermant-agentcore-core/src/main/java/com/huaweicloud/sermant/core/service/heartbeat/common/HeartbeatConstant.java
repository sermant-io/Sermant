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

package com.huaweicloud.sermant.core.service.heartbeat.common;

/**
 * 心跳发送服务涉及到的常量
 *
 * @author luanwenfei
 * @since 2022-03-28
 */
public class HeartbeatConstant {
    /**
     * 心跳间隔
     */
    public static final long INTERVAL = 3000L;

    /**
     * 最小心跳间隔
     */
    public static final long HEARTBEAT_MINIMAL_INTERVAL = 1000L;

    private HeartbeatConstant() {
    }
}
