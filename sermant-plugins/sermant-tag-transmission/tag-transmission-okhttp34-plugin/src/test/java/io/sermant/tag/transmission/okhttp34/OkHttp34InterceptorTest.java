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

package io.sermant.tag.transmission.okhttp34;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.okhttp34.interceptors.OkHttp34Interceptor;

import okhttp3.Request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OkHttp3.x,4.x tag transmission plugin interceptor test class.
 * When class is annotated with {@code @Execution(ExecutionMode.CONCURRENT)},
 * each test case will be executed in a different thread.
 * @since 2025-05-30
 */
@Execution(ExecutionMode.CONCURRENT)
public class OkHttp34InterceptorTest extends BaseInterceptorTest {
    private final OkHttp34Interceptor interceptor = new OkHttp34Interceptor();

    @Test
    public void testBeforeWithNoHeadersAndNoTags() {
        Map<String, List<String>> originHeaders = Collections.emptyMap();
        Map<String, List<String>> tags = Collections.emptyMap();
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);
        new Thread(() -> interceptor.before(context)).start();

        Request request = ((Request.Builder) resContext.getObject()).url("https://test.domain").build();
        assertThat(request.headers().size()).isEqualTo(0);
    }

    @Test
    public void testBeforeWithHeadersButNoTags() {
        Map<String, List<String>> originHeaders = Collections.singletonMap("defaultKey", Collections.singletonList("defaultValue"));
        Map<String, List<String>> tags = Collections.emptyMap();
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        Request request = ((Request.Builder) resContext.getObject()).url("https://test.domain").build();
        assertThat(request.headers().size()).isEqualTo(1);
        assertThat(request.header("defaultKey")).isEqualTo("defaultValue");
    }

    @Test
    public void testBeforeWithHeadersAndTags() {
        Map<String, List<String>> originHeaders = Collections.singletonMap("defaultKey", Collections.singletonList("defaultValue"));
        Map<String, List<String>> tags = Collections.singletonMap("id", Arrays.asList("testId001", "testId002"));
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        Request request = ((Request.Builder) resContext.getObject()).url("https://test.domain").build();
        assertThat(request.headers().size()).isEqualTo(3);
        assertThat(request.header("defaultKey")).isEqualTo("defaultValue");
        assertThat(request.header("id")).isIn("testId001", "testId002");
        assertThat(request.headers("id")).containsExactlyInAnyOrder("testId001", "testId002");
    }

    @Test
    public void testBeforeWithTagTransmissionConfigDisabled() {
        Map<String, List<String>> originHeaders = Collections.emptyMap();
        Map<String, List<String>> tags = Collections.singletonMap("id", Arrays.asList("testId001", "testId002"));
        tagTransmissionConfig.setEnabled(false);
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        Request request = ((Request.Builder) resContext.getObject()).url("https://test.domain").build();
        assertThat(request.headers().size()).isEqualTo(0);
        tagTransmissionConfig.setEnabled(true);
    }

    @Test
    public void testBeforeWithExistingTagHeader() {
        Map<String, List<String>> originHeaders = Collections.singletonMap("id", Collections.singletonList("existingValue"));
        Map<String, List<String>> tags = Collections.singletonMap("id", Collections.singletonList("tagValue"));
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        Request request = ((Request.Builder) resContext.getObject()).url("https://test.domain").build();
        assertThat(request.headers().size()).isEqualTo(1);
        assertThat(request.header("id")).isEqualTo("existingValue");
    }

    @Test
    public void testTagNotMatch() {
        Map<String, List<String>> originHeaders = Collections.singletonMap("id", Collections.singletonList("existingValue"));
        Map<String, List<String>> tags = Collections.singletonMap("notExistTag", Collections.singletonList("tagValue"));
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        Request request = ((Request.Builder) resContext.getObject()).url("https://test.domain").build();
        assertThat(request.headers().size()).isEqualTo(1);
        assertThat(request.header("id")).isEqualTo("existingValue");
    }

    @Test
    public void testBeforeWithBoundaryTag() {
        Map<String, List<String>> originHeaders = Collections.emptyMap();
        Map<String, List<String>> tags = Collections.singletonMap("id", Collections.emptyList());
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);

        Request request = ((Request.Builder) resContext.getObject()).url("https://test.domain").build();
        assertThat(request.header("id")).isNull();
    }

    @Test
    public void testBeforeTwiceInSameThread() {
        Map<String, List<String>> originHeaders = Collections.singletonMap("id", Collections.singletonList("existingValue"));
        Map<String, List<String>> tags = Collections.singletonMap("id", Collections.singletonList("tagValue"));
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        ExecuteContext resContext = interceptor.before(context);
        resContext = interceptor.before(resContext);

        Request request = ((Request.Builder) resContext.getObject()).url("https://test.domain").build();
        assertThat(request.headers().size()).isEqualTo(1);
        assertThat(request.header("id")).isEqualTo("existingValue");
    }

    @Test
    public void testAfter() {
        ExecuteContext context = ExecuteContext.forMemberMethod(new Request.Builder(), null, null, null, null);

        ExecuteContext resContext = interceptor.after(context);

        assertThat(resContext.getObject()).isEqualTo(context.getObject());
    }

    private ExecuteContext buildContext(Map<String, List<String>> originHeaders) {
        Request.Builder builder = new Request.Builder();
        for (Map.Entry<String, List<String>> entry : originHeaders.entrySet()) {
            for (String val : entry.getValue()) {
                builder.addHeader(entry.getKey(), val);
            }
        }

        return ExecuteContext.forMemberMethod(builder, null, null, null, null);
    }
}
