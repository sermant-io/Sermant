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

package com.huawei.discovery.entity;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.huawei.discovery.interceptors.BaseTest;

/**
 * PlugEffectStategyCache动态配置缓存测试
 *
 * @author chengyouling
 * @since 2022-10-12
 */
public class PlugEffectStategyCacheTest extends BaseTest {
    @Test
    public void testResolve() {
        JSONObject jobject = new JSONObject();
        jobject.put("strategy", "all");
        jobject.put("value", "service1");
        PlugEffectStategyCache.INSTANCE.resolve(jobject.toJSONString());
        Assert.assertEquals(PlugEffectStategyCache.INSTANCE.getConfigContent("strategy"), "all");
    }
}
