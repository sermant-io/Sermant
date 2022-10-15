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

package com.huawei.discovery.utils;

import static org.junit.Assert.*;

import com.huawei.discovery.entity.HttpAsyncContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * 工具类测试
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class HttpAsyncUtilsTest {
    @After
    public void tearDown() throws Exception {
        HttpAsyncUtils.remove();
    }

    @Test
    public void getOrCreateContext() {
        final HttpAsyncContext orCreateContext = HttpAsyncUtils.getOrCreateContext();
        Assert.assertNotNull(orCreateContext);
    }

    @Test
    public void saveHandler() {
        final Object handler = new Object();
        HttpAsyncUtils.saveHandler(handler);
        Assert.assertEquals(handler, HttpAsyncUtils.getOrCreateContext().getHandler());
    }

    @Test
    public void getContext() {
        HttpAsyncUtils.remove();
        Assert.assertNull(HttpAsyncUtils.getContext());
    }
}
