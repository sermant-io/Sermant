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

package com.huawei.dynamic.config.resolver;

import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

/**
 * 测试默认解析
 *
 * @author zhouss
 * @since 2022-04-16
 */
public class DefaultConfigResolverTest {
    @Test
    public void resolve() {
        final DynamicConfigEvent event = Mockito.mock(DynamicConfigEvent.class);
        Mockito.when(event.getContent()).thenReturn("test: 'hello world'");
        final DefaultConfigResolver defaultConfigResolver = new DefaultConfigResolver();
        final Map<String, Object> resolve = defaultConfigResolver.resolve(event);
        Assert.assertEquals(resolve.get("test"), "hello world");
    }
}
