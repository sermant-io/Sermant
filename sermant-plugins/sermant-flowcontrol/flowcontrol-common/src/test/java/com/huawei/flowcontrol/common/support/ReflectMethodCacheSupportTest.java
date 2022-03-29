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

package com.huawei.flowcontrol.common.support;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * 方法缓存测试
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class ReflectMethodCacheSupportTest {
    /**
     * 测试方法缓存
     *
     * @throws Exception 获取方法失败抛出
     */
    @Test
    public void testCache() throws Exception {
        final ReflectMethodCacheSupport reflectMethodCacheSupport = new ReflectMethodCacheSupport();
        String methodName = "test";
        final Method toString = reflectMethodCacheSupport.getInvokerMethod(methodName, fn -> {
            try {
                return Object.class.getDeclaredMethod("toString");
            } catch (NoSuchMethodException ignored) {
                // ignored
            }
            return null;
        }).get();
        Assert.assertNotNull(toString.invoke(reflectMethodCacheSupport));
    }
}
