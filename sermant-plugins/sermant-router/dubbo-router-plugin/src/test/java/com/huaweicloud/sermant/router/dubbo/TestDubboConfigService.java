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

package com.huaweicloud.sermant.router.dubbo;

import com.huaweicloud.sermant.router.dubbo.service.DubboConfigService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 测试
 *
 * @author provenceee
 * @since 2023-02-27
 */
public class TestDubboConfigService implements DubboConfigService {
    private boolean returnEmptyWhenGetMatchKeys;

    private boolean returnEmptyWhenGetMatchTags;

    @Override
    public void init(String cacheName, String serviceName) {
    }

    @Override
    public Set<String> getMatchKeys() {
        if (returnEmptyWhenGetMatchKeys) {
            return Collections.emptySet();
        }
        Set<String> keys = new HashSet<>();
        keys.add("bar");
        keys.add("foo");
        return keys;
    }

    @Override
    public Set<String> getMatchTags() {
        if (returnEmptyWhenGetMatchTags) {
            return Collections.emptySet();
        }
        Set<String> keys = new HashSet<>();
        keys.add("bar");
        keys.add("foo");
        return keys;
    }

    public void setReturnEmptyWhenGetMatchKeys(boolean returnEmptyWhenGetMatchKeys) {
        this.returnEmptyWhenGetMatchKeys = returnEmptyWhenGetMatchKeys;
    }

    public void setReturnEmptyWhenGetMatchTags(boolean returnEmptyWhenGetMatchTags) {
        this.returnEmptyWhenGetMatchTags = returnEmptyWhenGetMatchTags;
    }
}