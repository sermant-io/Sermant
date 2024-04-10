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

package com.huawei.dynamic.config.sources;

import com.huawei.dynamic.config.ConfigSource;

import java.util.Collections;
import java.util.Set;

/**
 * test priority is used
 *
 * @author zhouss
 * @since 2022-04-16
 */
public class TestConfigSources implements ConfigSource {
    public static final int ORDER = Integer.MIN_VALUE;

    @Override
    public Set<String> getConfigNames() {
        return Collections.singleton("test");
    }

    @Override
    public Object getConfig(String key) {
        if (key.equals("test")) {
            return ORDER;
        }
        return null;
    }

    @Override
    public int order() {
        return ORDER;
    }
}
