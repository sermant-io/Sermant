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

package com.huaweicloud.sermant.service;

import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.entity.InstanceInfo;

/**
 * 离群事件采集服务
 *
 * @author zhp
 * @since 2023-02-27
 */
public interface RemovalEventService extends PluginService {
    /**
     * 实例摘除事件上报
     *
     * @param info 实例信息
     */
    void reportRemovalEvent(InstanceInfo info);

    /**
     * 实例恢复事件上报
     *
     * @param info 实例信息
     */
    void reportRecoveryEvent(InstanceInfo info);
}
