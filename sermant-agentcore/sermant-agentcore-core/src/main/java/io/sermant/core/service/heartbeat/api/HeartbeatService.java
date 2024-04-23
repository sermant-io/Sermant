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

package io.sermant.core.service.heartbeat.api;

import io.sermant.core.service.BaseService;

/**
 * Heartbeat service
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public interface HeartbeatService extends BaseService {

    /**
     * plugin name
     */
    String PLUGIN_NAME_KEY = "pluginName";

    /**
     * plugin version
     */
    String PLUGIN_VERSION_KEY = "pluginVersion";

    /**
     * set additional information
     *
     * @param extInfoProvider extInfoProvider
     */
    void setExtInfo(ExtInfoProvider extInfoProvider);
}
