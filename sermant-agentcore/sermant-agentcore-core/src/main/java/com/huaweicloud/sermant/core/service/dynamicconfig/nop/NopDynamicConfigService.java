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

package com.huaweicloud.sermant.core.service.dynamicconfig.nop;

import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This class is for testing purpose only.
 *
 * @since 2021-12-31
 */
@Deprecated
public class NopDynamicConfigService extends DynamicConfigService {
    @Override
    protected Optional<String> doGetConfig(String key, String group) {
        return Optional.of("");
    }

    @Override
    protected boolean doPublishConfig(String key, String group, String content) {
        return true;
    }

    @Override
    protected boolean doRemoveConfig(String key, String group) {
        return true;
    }

    @Override
    protected boolean doAddConfigListener(String key, String group, DynamicConfigListener listener) {
        return true;
    }

    @Override
    protected boolean doRemoveConfigListener(String key, String group) {
        return true;
    }

    @Override
    protected List<String> doListKeysFromGroup(String group) {
        return Collections.emptyList();
    }

    @Override
    protected boolean doAddGroupListener(String group, DynamicConfigListener listener) {
        return true;
    }

    @Override
    protected boolean doRemoveGroupListener(String group) {
        return true;
    }
}
