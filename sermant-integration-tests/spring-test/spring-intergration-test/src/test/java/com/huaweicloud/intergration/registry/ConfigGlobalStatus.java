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

package com.huaweicloud.intergration.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局配置发布状态
 *
 * @author zhouss
 * @since 2022-10-26
 */
public enum ConfigGlobalStatus {
    /**
     * 单例
     */
    INSTANCE;

    private final Map<String, Boolean> statsCache = new ConcurrentHashMap<>();

    public void saveOpenSate(String type) {
        statsCache.put(type, Boolean.TRUE);
    }

    public boolean isOpen(String type) {
        return statsCache.get(type) != null;
    }
}
