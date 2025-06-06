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

package io.sermant.tag.transmission.jakarta.servlet.interceptors;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.tag.TrafficTag;
import io.sermant.core.utils.tag.TrafficUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;

/**
 * Test for JakartaServletInterceptor
 *
 * @since 2025-05-22
 */
public class JakartaServletInterceptorTest extends BaseInterceptorTest {

    private final JakartaServletInterceptor interceptor;
    private final Object[] arguments;

    public JakartaServletInterceptorTest() {
        interceptor = new JakartaServletInterceptor();
        arguments = new Object[2];
    }

    @Test
    public void testExtractSingleTag() {
        Map<String, List<String>> addHeaders = Collections.singletonMap("id", Collections.singletonList("testId001"));
        ExecuteContext context = buildContext(addHeaders);

        interceptor.before(context);

        assertThat(TrafficUtils.getTrafficTag()).extracting(TrafficTag::getTag).isEqualTo(addHeaders);
        interceptor.after(context);
    }

    @Test
    public void testExtractMultipleTags() {
        Map<String, List<String>> addHeaders = Collections.singletonMap("id", Arrays.asList("testId001", "testId002"));
        ExecuteContext context = buildContext(addHeaders);

        interceptor.before(context);

        assertThat(TrafficUtils.getTrafficTag()).extracting(TrafficTag::getTag).isEqualTo(addHeaders);
        interceptor.after(context);
    }

    @Test
    public void testExtractDifferentTags1() {
        Map<String, List<String>> addHeaders = Collections.singletonMap("name", Arrays.asList("testName001", "testName002"));
        ExecuteContext context = buildContext(addHeaders);

        interceptor.before(context);

        assertThat(TrafficUtils.getTrafficTag()).extracting(TrafficTag::getTag).isEqualTo(addHeaders);
        interceptor.after(context);
    }

    @Test
    public void testExtractDifferentTags2() {
        Map<String, List<String>> addHeaders = Collections.singletonMap("address", Arrays.asList("address001", "address001"));
        ExecuteContext context = buildContext(addHeaders);

        interceptor.before(context);

        assertThat(TrafficUtils.getTrafficTag()).isNull();
        interceptor.after(context);
    }

    @Test
    public void testExtractDifferentTags3() {
        Map<String, List<String>> addHeaders = new HashMap<>();
        addHeaders.put("name", Arrays.asList("testName001", "testName002"));
        addHeaders.put("address", Arrays.asList("address001", "address001"));
        ExecuteContext context = buildContext(addHeaders);

        interceptor.before(context);

        assertThat(TrafficUtils.getTrafficTag()).extracting(TrafficTag::getTag)
            .isEqualTo(Collections.singletonMap("name", Arrays.asList("testName001", "testName002")));
        interceptor.after(context);
    }

    @Test
    public void testBoundary1() {
        Map<String, List<String>> addHeaders = new HashMap<>();
        addHeaders.put("name", Collections.emptyList());
        ExecuteContext context = buildContext(addHeaders);

        interceptor.before(context);

        assertThat(TrafficUtils.getTrafficTag())
            .extracting(TrafficTag::getTag)
            .extracting(tag -> tag.get("name"))
            .isNull();
        interceptor.after(context);
    }

    @Test
    public void testBoundary2() {
        Map<String, List<String>> addHeaders = new HashMap<>();
        addHeaders.put("name", null);
        ExecuteContext context = buildContext(addHeaders);

        interceptor.before(context);

        assertThat(TrafficUtils.getTrafficTag())
            .extracting(TrafficTag::getTag)
            .extracting(tag -> tag.get("name"))
            .isNull();
        interceptor.after(context);
    }

    @Test
    public void testTagCleanup() {
        Map<String, List<String>> addHeaders = Collections.singletonMap("id", Collections.singletonList("testId001"));
        ExecuteContext context = buildContext(addHeaders);

        interceptor.before(context);
        interceptor.after(context);

        assertThat(TrafficUtils.getTrafficTag()).isNull();
    }

    @Test
    public void testDisabledTagTransmission() {
        Map<String, List<String>> addHeaders = Collections.singletonMap("id", Collections.singletonList("testId001"));
        tagTransmissionConfig.setEnabled(false);
        ExecuteContext context = buildContext(addHeaders);

        interceptor.before(context);

        assertThat(TrafficUtils.getTrafficTag()).isNull();
        interceptor.after(context);
    }

    public ExecuteContext buildContext(Map<String, List<String>> addHeaders) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn(Collections.enumeration(addHeaders.keySet())).when(request).getHeaderNames();
        for (Map.Entry<String, List<String>> entry : addHeaders.entrySet()) {
            Enumeration<String> returnValue = entry.getValue() == null ? null : Collections.enumeration(entry.getValue());
            doReturn(returnValue).when(request).getHeaders(eq(entry.getKey()));
        }

        arguments[0] = request;
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }
}
