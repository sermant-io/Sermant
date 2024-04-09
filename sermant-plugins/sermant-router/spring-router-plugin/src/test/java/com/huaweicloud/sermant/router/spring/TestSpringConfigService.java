/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.router.spring;

import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Test Configuration Service
 *
 * @author provenceee
 * @since 2022-09-08
 */
public class TestSpringConfigService implements SpringConfigService {
    private String cacheName;

    private String serviceName;

    private boolean returnEmptyWhenGetMatchKeys;

    private boolean returnEmptyWhenGetMatchTags;

    @Override
    public void init(String cacheName, String serviceName) {
        this.cacheName = cacheName;
        this.serviceName = serviceName;
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
    public Set<String> getInjectTags() {
        if (returnEmptyWhenGetMatchTags) {
            return Collections.emptySet();
        }
        Set<String> tags = new HashSet<>();
        tags.add("bar");
        tags.add("foo");
        return tags;
    }

    public String getCacheName() {
        return cacheName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setReturnEmptyWhenGetMatchKeys(boolean returnEmptyWhenGetMatchKeys) {
        this.returnEmptyWhenGetMatchKeys = returnEmptyWhenGetMatchKeys;
    }

    public void setReturnEmptyWhenGetMatchTags(boolean returnEmptyWhenGetMatchTags) {
        this.returnEmptyWhenGetMatchTags = returnEmptyWhenGetMatchTags;
    }
}