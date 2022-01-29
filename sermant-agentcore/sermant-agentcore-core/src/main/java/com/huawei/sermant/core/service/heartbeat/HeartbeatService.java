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

package com.huawei.sermant.core.service.heartbeat;

import com.huawei.sermant.core.service.BaseService;

/**
 * 心跳服务
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public interface HeartbeatService extends BaseService {

    /**
     * 心跳的插件名称键
     */
    String PLUGIN_NAME_KEY = "pluginName";

    /**
     * 心跳的插件版本键
     */
    String PLUGIN_VERSION_KEY = "pluginVersion";

    /**
     * 设置额外信息
     *
     * @param extInfoProvider 其他信息的提供者
     */
    void setExtInfo(ExtInfoProvider extInfoProvider);
}
