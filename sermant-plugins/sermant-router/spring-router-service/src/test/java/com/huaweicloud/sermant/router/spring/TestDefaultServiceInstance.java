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

package com.huaweicloud.sermant.router.spring;

import org.springframework.cloud.client.DefaultServiceInstance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试ServiceInstance
 *
 * @author provenceee
 * @since 2022-09-13
 */
public class TestDefaultServiceInstance extends DefaultServiceInstance {
    public TestDefaultServiceInstance(Map<String, String> metadata) {
        super("foo", "bar", "bar", 8080, false, Collections.unmodifiableMap(metadata));
    }

    public static TestDefaultServiceInstance getTestDefaultServiceInstance(String version) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("version", version);
        return new TestDefaultServiceInstance(metadata);
    }
}