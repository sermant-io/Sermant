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

package io.sermant.flowcontrol.common.config;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * XdsFlowControlConfig Test
 *
 * @since 2024-12-10
 * @author zhp
 */
public class XdsFlowControlConfigTest {

    private XdsFlowControlConfig xdsFlowControlConfigUnderTest;

    @Before
    public void setUp() throws Exception {
        xdsFlowControlConfigUnderTest = new XdsFlowControlConfig();
    }

    @Test
    public void testRetryStatusCodesGetterAndSetter() {
        final List<String> retryStatusCodes = Collections.singletonList("value");
        xdsFlowControlConfigUnderTest.setRetryStatusCodes(retryStatusCodes);
        assertEquals(retryStatusCodes, xdsFlowControlConfigUnderTest.getRetryStatusCodes());
    }

    @Test
    public void testRetryHeaderNamesGetterAndSetter() {
        final List<String> retryHeaderNames = Collections.singletonList("value");
        xdsFlowControlConfigUnderTest.setRetryHeaderNames(retryHeaderNames);
        assertEquals(retryHeaderNames, xdsFlowControlConfigUnderTest.getRetryHeaderNames());
    }

    @Test
    public void testEnableGetterAndSetter() {
        final boolean enable = false;
        xdsFlowControlConfigUnderTest.setEnable(enable);
        assertFalse(xdsFlowControlConfigUnderTest.isEnable());
    }
}
