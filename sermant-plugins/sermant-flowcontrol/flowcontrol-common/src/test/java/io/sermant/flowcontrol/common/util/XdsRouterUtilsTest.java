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

package io.sermant.flowcontrol.common.util;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.service.xds.entity.XdsLocality;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * XdsRouterUtilTest
 *
 * @author zhp
 * @since 2024-12-10
 **/
public class XdsRouterUtilsTest extends XdsAbstractTest {
    @Test
    public void testGetLocalityInfoOfSelfService() {
        // not find matched service instance
        ServiceMeta meta = new ServiceMeta();
        meta.setService("consumer");
        Mockito.when(ConfigManager.getConfig(ServiceMeta.class)).thenReturn(meta);
        Optional<XdsLocality> localityInfo = XdsRouterUtils.getLocalityInfoOfSelfService();
        Assert.assertFalse(localityInfo.isPresent());

        // find matched service instance
        meta.setService("provider");
        XdsRouterUtils.updateLocalityObtainedFlag(false);
        localityInfo = XdsRouterUtils.getLocalityInfoOfSelfService();
        Assert.assertTrue(localityInfo.isPresent());
        Assert.assertEquals("127.0.0.1", localityInfo.get().getRegion());
    }
}
