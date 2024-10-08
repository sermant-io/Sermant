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

package io.sermant.tag.transmission.sofarpc.interceptors;

import io.sermant.core.utils.tag.TrafficTag;

import org.junit.Before;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AbstractRpcInterceptorTest
 *
 * @author daizhenyu
 * @since 2023-09-06
 **/
public abstract class AbstractRpcInterceptorTest extends BaseInterceptorTest {
    // Create a key-value relationship for traffic labels to build parameters
    public Map<String, List<String>> fullTrafficTag;

    public AbstractRpcInterceptorTest() {
    }

    @Before
    public void beforeTest() {
        fullTrafficTag = new HashMap<>();
        fullTrafficTag.put("id", Collections.singletonList("001"));
        fullTrafficTag.put("name", Collections.singletonList("test001"));

        // initialize traffic tag
        Map<String, List<String>> tag = new HashMap<>();
        tag.put("id", Collections.singletonList("001"));
        tag.put("name", Collections.singletonList("test001"));
        fullTrafficTag = new HashMap<>(tag);
        TrafficTag trafficTag = new TrafficTag(tag);
        doBefore(trafficTag);
    }

    public abstract void doBefore(TrafficTag trafficTag);

    public Map<String, List<String>> buildExpectTrafficTag(String... keys) {
        Map<String, List<String>> expectTag = new HashMap<>();
        for (String key : keys) {
            expectTag.put(key, fullTrafficTag.get(key));
        }
        return expectTag;
    }
}
