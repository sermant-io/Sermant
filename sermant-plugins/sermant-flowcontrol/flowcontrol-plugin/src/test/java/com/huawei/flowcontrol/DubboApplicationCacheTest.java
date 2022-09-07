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

package com.huawei.flowcontrol;

import org.junit.Assert;
import org.junit.Test;

/**
 * 缓存简单测试
 *
 * @author zhouss
 * @since 2022-09-13
 */
public class DubboApplicationCacheTest {
    @Test
    public void cache() {
        String interfaceName = "com.test.dubbo.cache";
        String application = this.getClass().getName();
        DubboApplicationCache.INSTANCE.cache(interfaceName, application);
        Assert.assertEquals(DubboApplicationCache.INSTANCE.getApplicationCache().get(interfaceName), application);
        DubboApplicationCache.INSTANCE.getApplicationCache().clear();
    }
}
