/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
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

package io.sermant.xds.common.config;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author daizhenyu
 * @since 2025-03-17
 **/
public class XdsTrafficManagementConfigTest extends TestCase {
    private XdsTrafficManagementConfig xdsTrafficManagementConfig;

    @Before
    public void setUp() {
        xdsTrafficManagementConfig = new XdsTrafficManagementConfig();
    }

    @Test
    public void testRetryStatusCodesGetterAndSetter() {
        final List<String> retryStatusCodes = Collections.singletonList("value");
        xdsTrafficManagementConfig.setRetryStatusCodes(retryStatusCodes);
        assertEquals(retryStatusCodes, xdsTrafficManagementConfig.getRetryStatusCodes());
    }

    @Test
    public void testRetryHeaderNamesGetterAndSetter() {
        final List<String> retryHeaderNames = Collections.singletonList("value");
        xdsTrafficManagementConfig.setRetryHeaderNames(retryHeaderNames);
        assertEquals(retryHeaderNames, xdsTrafficManagementConfig.getRetryHeaderNames());
    }

    @Test
    public void testEnableGetterAndSetter() {
        final boolean enable = false;
        xdsTrafficManagementConfig.setEnable(enable);
        assertFalse(xdsTrafficManagementConfig.isEnable());
    }
}
