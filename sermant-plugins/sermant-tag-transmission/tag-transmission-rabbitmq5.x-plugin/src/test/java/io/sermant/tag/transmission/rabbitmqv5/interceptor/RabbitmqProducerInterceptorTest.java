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

package io.sermant.tag.transmission.rabbitmqv5.interceptor;

import com.rabbitmq.client.AMQP;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.tag.TrafficUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RabbitMQ5.x tag transmission test
 *
 * @since 2025-07-05
 */
public class RabbitmqProducerInterceptorTest extends BaseInterceptorTest {
    private final RabbitmqProducerInterceptor interceptor = new RabbitmqProducerInterceptor();

    @Test
    public void testBeforeWithNoHeadersAndNoTags() {
        Map<String, Object> originHeaders = Collections.emptyMap();
        Map<String, List<String>> tags = Collections.emptyMap();
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        interceptor.before(context);

        AMQP.BasicProperties properties = (AMQP.BasicProperties) context.getArguments()[4];
        assertThat(properties.getHeaders()).isEmpty();
    }

    @Test
    public void testBeforeWithHeadersButNoTags() {
        Map<String, Object> originHeaders = Collections.singletonMap("defaultKey", "defaultValue");
        Map<String, List<String>> tags = Collections.emptyMap();
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        interceptor.before(context);

        AMQP.BasicProperties properties = (AMQP.BasicProperties) context.getArguments()[4];
        assertThat(properties.getHeaders()).containsEntry("defaultKey", "defaultValue");
    }

    @Test
    public void testBeforeWithHeadersAndTags() {
        Map<String, Object> originHeaders = Collections.singletonMap("defaultKey", "defaultValue");
        Map<String, List<String>> tags = Collections.singletonMap("id", Arrays.asList("testId001", "testId002"));
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        interceptor.before(context);

        AMQP.BasicProperties properties = (AMQP.BasicProperties) context.getArguments()[4];
        assertThat(properties.getHeaders().size()).isEqualTo(2);
        assertThat(properties.getHeaders()).containsEntry("defaultKey", "defaultValue");
        assertThat(properties.getHeaders()).containsEntry("id", "testId001");
    }

    @Test
    public void testBeforeWithTagTransmissionConfigDisabled() {
        Map<String, Object> originHeaders = Collections.emptyMap();
        Map<String, List<String>> tags = Collections.singletonMap("id", Arrays.asList("testId001", "testId002"));
        tagTransmissionConfig.setEnabled(false);
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        interceptor.before(context);

        AMQP.BasicProperties properties = (AMQP.BasicProperties) context.getArguments()[4];
        assertThat(properties.getHeaders().size()).isEqualTo(0);
        tagTransmissionConfig.setEnabled(true);
    }

    @Test
    public void testBeforeWithExistingTagHeader() {
        Map<String, Object> originHeaders = Collections.singletonMap("id", "existingValue");
        Map<String, List<String>> tags = Collections.singletonMap("id", Collections.singletonList("tagValue"));
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        interceptor.before(context);

        AMQP.BasicProperties properties = (AMQP.BasicProperties) context.getArguments()[4];
        assertThat(properties.getHeaders()).containsEntry("id", "existingValue");
    }

    @Test
    public void testTagNotMatch() {
        Map<String, Object> originHeaders = Collections.singletonMap("id", "existingValue");
        Map<String, List<String>> tags = Collections.singletonMap("notExistTag", Collections.singletonList("tagValue"));
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        interceptor.before(context);

        AMQP.BasicProperties properties = (AMQP.BasicProperties) context.getArguments()[4];
        assertThat(properties.getHeaders()).containsEntry("id", "existingValue");
        assertThat(properties.getHeaders()).doesNotContainKey("notExistTag");
    }

    @Test
    public void testBeforeWithBoundaryTag() {
        Map<String, Object> originHeaders = Collections.emptyMap();
        Map<String, List<String>> tags = Collections.singletonMap("id", Collections.emptyList());
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        interceptor.before(context);

        AMQP.BasicProperties properties = (AMQP.BasicProperties) context.getArguments()[4];
        assertThat(properties.getHeaders().get("id")).isNull();
    }

    @Test
    public void testBeforeWithMultipleTags() {
        Map<String, Object> originHeaders = Collections.emptyMap();
        Map<String, List<String>> tags = new HashMap<>();
        tags.put("id", Arrays.asList("testId001", "testId002"));
        tags.put("name", Collections.singletonList("testName001"));
        ExecuteContext context = buildContext(originHeaders);
        TrafficUtils.updateTrafficTag(tags);

        interceptor.before(context);

        AMQP.BasicProperties properties = (AMQP.BasicProperties) context.getArguments()[4];
        assertThat(properties.getHeaders()).containsEntry("id", "testId001");
        assertThat(properties.getHeaders()).containsEntry("name", "testName001");
    }

    @Test
    public void testBeforeWithNullProperty() {
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), null, new Object[5], null, null);
        Map<String, List<String>> tags = Collections.singletonMap("id", Collections.singletonList("testId001"));
        TrafficUtils.updateTrafficTag(tags);

        interceptor.before(context);

        AMQP.BasicProperties properties = (AMQP.BasicProperties) context.getArguments()[4];
        assertThat(properties.getHeaders()).containsEntry("id", "testId001");
    }

    @Test
    public void testDoAfter() {
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), null, null, null, null);

        ExecuteContext resContext = interceptor.doAfter(context);

        assertThat(resContext.getObject()).isEqualTo(context.getObject());
    }

    private ExecuteContext buildContext(Map<String, Object> originHeaders) {
        Object[] arguments = new Object[5];
        arguments[4] = createProperties(originHeaders);
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }

    private AMQP.BasicProperties createProperties(Map<String, Object> headers) {
        return new AMQP.BasicProperties.Builder()
            .headers(headers)
            .build();
    }
}
