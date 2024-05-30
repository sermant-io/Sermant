/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.xds.cache;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * XdsDataCache UT
 *
 * @author daizhenyu
 * @since 2024-05-24
 **/
public class XdsDataCacheTest {
    @Test
    public void testGetClustersByServiceName() {
        Map<String, Set<String>> mapping = new HashMap<>();
        Set<String> clusters = new HashSet<>();
        clusters.add("cluster");
        mapping.put("service-A", clusters);
        Set<String> result;

        // serviceNameMapping is null
        result = XdsDataCache.getClustersByServiceName("service-A");
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());

        // serviceNameMapping is not null, get un cached service
        XdsDataCache.updateServiceNameMapping(mapping);
        result = XdsDataCache.getClustersByServiceName("service-B");
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());

        // serviceNameMapping is not null, get cached service
        XdsDataCache.updateServiceNameMapping(mapping);
        result = XdsDataCache.getClustersByServiceName("service-A");
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains("cluster"));
    }
}