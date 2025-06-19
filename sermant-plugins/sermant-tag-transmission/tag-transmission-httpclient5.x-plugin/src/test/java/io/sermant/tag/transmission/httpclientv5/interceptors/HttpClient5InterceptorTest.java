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

package io.sermant.tag.transmission.httpclientv5.interceptors;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.tag.TrafficUtils;

import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicHttpRequest;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HttpClient5 tag transmission plugin interceptor test class.
 *
 * @since 2025-06-24
 */
public class HttpClient5InterceptorTest extends BaseInterceptorTest {
    private final HttpClient5Interceptor interceptor = new HttpClient5Interceptor();

    @Test
    public void testBeforeWithNoHeadersAndNoTags() {
        Map<String, List<String>> originHeaders = Collections.emptyMap();
        Map<String, List<String>> tags = Collections.emptyMap();
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        HttpRequest request = (HttpRequest) resContext.getArguments()[1];
        assertThat(request.getHeaders()).isEmpty();
    }

    @Test
    public void testBeforeWithHeadersButNoTags() {
        Map<String, List<String>> originHeaders = Collections.singletonMap("defaultKey", Collections.singletonList("defaultValue"));
        Map<String, List<String>> tags = Collections.emptyMap();
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        HttpRequest request = (HttpRequest) resContext.getArguments()[1];
        assertThat(request.getHeaders("defaultKey")).hasSize(1);
        assertThat(request.getFirstHeader("defaultKey").getValue()).isEqualTo("defaultValue");
    }

    @Test
    public void testBeforeWithHeadersAndTags() {
        Map<String, List<String>> originHeaders = Collections.singletonMap("defaultKey", Collections.singletonList("defaultValue"));
        Map<String, List<String>> tags = Collections.singletonMap("id", Arrays.asList("testId001", "testId002"));
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        HttpRequest request = (HttpRequest) resContext.getArguments()[1];
        assertThat(request.getHeaders("defaultKey")).hasSize(1);
        assertThat(request.getFirstHeader("defaultKey").getValue()).isEqualTo("defaultValue");
        assertThat(request.getHeaders("id")).hasSize(2);
        assertThat(request.getHeaders("id")).extracting(NameValuePair::getValue)
            .containsExactlyInAnyOrder("testId001", "testId002");
    }

    @Test
    public void testBeforeWithTagTransmissionConfigDisabled() {
        Map<String, List<String>> originHeaders = Collections.emptyMap();
        Map<String, List<String>> tags = Collections.singletonMap("id", Arrays.asList("testId001", "testId002"));
        tagTransmissionConfig.setEnabled(false);
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        HttpRequest request = (HttpRequest) resContext.getArguments()[1];
        assertThat(request.getHeaders()).isEmpty();
    }

    @Test
    public void testBeforeWithExistingTagHeader() {
        Map<String, List<String>> originHeaders = Collections.singletonMap("id", Collections.singletonList("existingValue"));
        Map<String, List<String>> tags = Collections.singletonMap("id", Collections.singletonList("tagValue"));
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        HttpRequest request = (HttpRequest) resContext.getArguments()[1];
        assertThat(request.getHeaders("id")).hasSize(1);
        assertThat(request.getFirstHeader("id").getValue()).isEqualTo("existingValue");
    }

    @Test
    public void testTagNotMatch() {
        Map<String, List<String>> originHeaders = Collections.singletonMap("id", Collections.singletonList("existingValue"));
        Map<String, List<String>> tags = Collections.singletonMap("notExistTag", Collections.singletonList("tagValue"));
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        HttpRequest request = (HttpRequest) resContext.getArguments()[1];
        assertThat(request.getHeaders("id")).hasSize(1);
        assertThat(request.getFirstHeader("id").getValue()).isEqualTo("existingValue");
        assertThat(request.getHeaders("notExistTag")).isEmpty();
    }

    @Test
    public void testBeforeWithBoundaryTag() {
        Map<String, List<String>> originHeaders = Collections.emptyMap();
        Map<String, List<String>> tags = Collections.singletonMap("id", Collections.emptyList());
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        HttpRequest request = (HttpRequest) resContext.getArguments()[1];
        assertThat(request.getFirstHeader("id").getValue()).isNull();
    }

    @Test
    public void testAfter() {
        ExecuteContext context = ExecuteContext.forMemberMethod(new BasicHttpRequest(Method.GET, "https://test.domain"), null, null, null, null);

        ExecuteContext resContext = interceptor.after(context);

        assertThat(resContext.getObject()).isEqualTo(context.getObject());
    }

    @Test
    public void testBeforeWithNonHttpRequestArgument() {
        Map<String, List<String>> tags = Collections.singletonMap("id", Collections.singletonList("tagValue"));
        TrafficUtils.updateTrafficTag(tags);
        Object[] arguments = new Object[2];
        arguments[1] = "not a HttpRequest";
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);

        ExecuteContext resContext = interceptor.before(context);

        assertThat(resContext).isEqualTo(context);
        assertThat(resContext.getArguments()[1]).isEqualTo("not a HttpRequest");
    }

    private ExecuteContext buildContext(Map<String, List<String>> originHeaders) {
        HttpRequest request = new BasicHttpRequest(Method.GET, "https://test.domain");
        for (Map.Entry<String, List<String>> entry : originHeaders.entrySet()) {
            for (String val : entry.getValue()) {
                request.addHeader(entry.getKey(), val);
            }
        }

        Object[] arguments = new Object[2];
        arguments[1] = request;
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }
}
