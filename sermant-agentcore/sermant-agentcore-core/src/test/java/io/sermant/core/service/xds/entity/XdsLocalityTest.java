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

package io.sermant.core.service.xds.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * XdsLocalityTest
 *
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class XdsLocalityTest {
    @Test
    public void testXdsLocality() {
        XdsLocality locality = new XdsLocality();
        locality.setRegion("region");
        locality.setZone("zone");
        locality.setSubZone("subzone");
        locality.setLocalityPriority(5);
        locality.setLoadBalanceWeight(5);
        Assert.assertEquals("region", locality.getRegion());
        Assert.assertEquals("zone", locality.getZone());
        Assert.assertEquals("subzone", locality.getSubZone());
        Assert.assertEquals(5, locality.getLocalityPriority());
        Assert.assertEquals(5, locality.getLoadBalanceWeight());
    }

    @Test
    public void testEquals() {
        XdsLocality comparedXdsLocality = new XdsLocality();
        comparedXdsLocality.setRegion("region");
        comparedXdsLocality.setZone("zone");
        comparedXdsLocality.setSubZone("subzone");

        // equals with null
        Assert.assertFalse(comparedXdsLocality.equals(null));

        // equal with zone is null
        XdsLocality xdsLocality = new XdsLocality();
        xdsLocality.setRegion("region");
        xdsLocality.setSubZone("subzone");
        Assert.assertFalse(comparedXdsLocality.equals(xdsLocality));

        // equal with same locality
        xdsLocality.setZone("zone");
        Assert.assertTrue(comparedXdsLocality.equals(xdsLocality));
    }
}
