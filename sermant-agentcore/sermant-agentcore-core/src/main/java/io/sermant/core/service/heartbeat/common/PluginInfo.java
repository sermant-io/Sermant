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

package io.sermant.core.service.heartbeat.common;

import java.util.Map;

/**
 * Plugin information
 *
 * @author luanwenfei
 * @since 2022-10-28
 */
public class PluginInfo {
    private String name;

    private String version;

    private Map<String, String> extInfo;

    /**
     * constructor
     *
     * @param name plugin name
     * @param version plugin version
     */
    public PluginInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public void setExtInfo(Map<String, String> extInfo) {
        this.extInfo = extInfo;
    }

    public Map<String, String> getExtInfo() {
        return extInfo;
    }
}
