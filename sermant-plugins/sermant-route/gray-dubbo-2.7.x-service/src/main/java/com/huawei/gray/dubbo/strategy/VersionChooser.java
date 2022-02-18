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

package com.huawei.gray.dubbo.strategy;

import com.huawei.gray.dubbo.strategy.version.MsgVersionStrategy;
import com.huawei.gray.dubbo.strategy.version.UrlVersionStrategy;
import com.huawei.route.common.gray.label.entity.VersionFrom;

import java.util.HashMap;
import java.util.Map;

/**
 * 版本选择器
 *
 * @author provenceee
 * @since 2021/12/8
 */
public enum VersionChooser {
    /**
     * 单例
     */
    INSTANCE;

    private final Map<String, VersionStrategy> map;

    VersionChooser() {
        map = new HashMap<String, VersionStrategy>();
        map.put(VersionFrom.REGISTER_MSG.name(), new MsgVersionStrategy());
        map.put(VersionFrom.REGISTER_URL.name(), new UrlVersionStrategy());
    }

    /**
     * 根据灰度配置选择一个版本策略
     *
     * @param versionFrom 版本来源
     * @return 版本策略
     */
    public VersionStrategy choose(VersionFrom versionFrom) {
        return map.get(versionFrom.name());
    }
}