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

package com.huawei.sermant.core.service.dynamicconfig.nop;

import java.util.Collections;
import java.util.List;

import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

/**
 * This class is for testing purpose only.
 */
@Deprecated
public class NopDynamicConfigService extends DynamicConfigService {
    @Override
    public String getConfig(String key, String group) {
        return "";
    }

    @Override
    public boolean publishConfig(String key, String group, String content) {
        return true;
    }

    @Override
    public boolean removeConfig(String key, String group) {
        return true;
    }

    @Override
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener) {
        return true;
    }

    @Override
    public boolean removeConfigListener(String key, String group) {
        return true;
    }

    @Override
    public List<String> listKeysFromGroup(String group) {
        return Collections.emptyList();
    }

    @Override
    public boolean addGroupListener(String group, DynamicConfigListener listener) {
        return true;
    }

    @Override
    public boolean removeGroupListener(String group) {
        return true;
    }
}
