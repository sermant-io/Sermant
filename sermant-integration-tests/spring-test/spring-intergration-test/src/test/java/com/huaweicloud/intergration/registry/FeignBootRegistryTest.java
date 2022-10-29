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

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * feign测试
 *
 * @author zhouss
 * @since 2022-10-26
 */
public class FeignBootRegistryTest extends BootRegistryTest {
    @Rule(order = 200)
    public final BootRegistryRule bootRegistryRule = new BootRegistryRule();
    
    /**
     * 此处将模拟请求两次, 基于轮询负载均衡将拿到两个不同结果
     */
    @Test
    public void testFeign() {
        check("feignRegistryPost", HttpMethod.GET);
        check("feignRegistry", HttpMethod.GET);
    }

    @Override
    protected String getUrl() {
        return "http://localhost:8015/bootRegistry";
    }

    @Override
    protected Map<String, String> getLabels() {
        final Map<String, String> labels = new HashMap<>();
        labels.put("app", getType());
        labels.put("environment", "development");
        return labels;
    }

    @Override
    protected String getType() {
        return "feign";
    }
}
