/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.auto.sc;

import static org.junit.Assert.*;

import com.netflix.loadbalancer.Server;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Get the meta-test
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class ServiceCombServerIntrospectorTest {
    @Test
    public void getMetadata() {
        final ServiceCombServerIntrospector introspector = new ServiceCombServerIntrospector();
        final Map<String, String> metadata = introspector.getMetadata(new Server("localhost:9090"));
        Assert.assertEquals(metadata, Collections.emptyMap());
        final ServiceCombServer serviceCombServer = Mockito.mock(ServiceCombServer.class);
        final HashMap<String, String> meta = new HashMap<>();
        Mockito.when(serviceCombServer.getMetadata()).thenReturn(meta);
        Assert.assertEquals(introspector.getMetadata(serviceCombServer), meta);
    }
}
