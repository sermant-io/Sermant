/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.backend.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 心跳请求数据
 *
 * @author xuezechao
 * @since 2022-02-28
 */
@Getter
@Setter
public class HeartbeatEntity {

    private String app;

    private String hostname;

    private long heartbeatVersion;

    private String pluginVersion;

    private long lastHeartbeat;

    private String pluginName;

    private String appType;

    private List<String> ip;

    private String version;

    private String instanceId;
}
