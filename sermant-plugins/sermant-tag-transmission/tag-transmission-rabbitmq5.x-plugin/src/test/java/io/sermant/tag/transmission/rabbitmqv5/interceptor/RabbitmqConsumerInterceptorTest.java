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
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.tag.TrafficTag;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.rabbitmqv5.wrapper.ConsumerWrapper;

import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test for RabbitMQ consumer interceptor
 *
 * @since 2025-07-05
 */
public class RabbitmqConsumerInterceptorTest extends BaseInterceptorTest {

    private final RabbitmqConsumerInterceptor interceptor;
    private final Object[] arguments;

    public RabbitmqConsumerInterceptorTest() {
        interceptor = new RabbitmqConsumerInterceptor();
        arguments = new Object[7];
    }

    @Test
    public void testDoBeforeWithValidConsumer() {
        Consumer mockConsumer = mock(Consumer.class);
        arguments[6] = mockConsumer;

        ExecuteContext context = buildContext(new Object(), arguments);
        ExecuteContext result = interceptor.doBefore(context);

        assertThat(result).isEqualTo(context);
        assertThat(arguments[6]).isInstanceOf(ConsumerWrapper.class);
    }

    @Test
    public void testDoAfter() {
        ExecuteContext context = buildContext(new Object(), null);

        ExecuteContext result = interceptor.doAfter(context);

        assertThat(result).isEqualTo(context);
    }

    @Test
    public void testExtractSingleTag() throws IOException {
        Map<String, Object> headers = Collections.singletonMap("id", "testId001");
        AMQP.BasicProperties properties = createProperties(headers);
        Consumer mockConsumer = mock(Consumer.class);
        arguments[6] = mockConsumer;
        ExecuteContext context = buildContext(new Object(), arguments);
        interceptor.doBefore(context);
        ConsumerWrapper wrapper = (ConsumerWrapper) arguments[6];

        wrapper.handleDelivery("consumerTag", mock(Envelope.class), properties, new byte[0]);

        TrafficTag trafficTag = TrafficUtils.getTrafficTag();
        assertThat(trafficTag).isNotNull();
        assertThat(trafficTag.getTag()).containsEntry("id", Collections.singletonList("testId001"));
    }

    @Test
    public void testExtractMultipleTags() throws IOException {
        Map<String, Object> headers = new HashMap<>();
        headers.put("id", "testId001");
        headers.put("name", "testName001");
        AMQP.BasicProperties properties = createProperties(headers);
        Consumer mockConsumer = mock(Consumer.class);
        arguments[6] = mockConsumer;
        ExecuteContext context = buildContext(new Object(), arguments);
        interceptor.doBefore(context);
        ConsumerWrapper wrapper = (ConsumerWrapper) arguments[6];

        wrapper.handleDelivery("consumerTag", mock(Envelope.class), properties, new byte[0]);

        TrafficTag trafficTag = TrafficUtils.getTrafficTag();
        assertThat(trafficTag).isNotNull();
        assertThat(trafficTag.getTag()).containsEntry("id", Collections.singletonList("testId001"));
        assertThat(trafficTag.getTag()).containsEntry("name", Collections.singletonList("testName001"));
    }

    @Test
    public void testExtractDifferentTags() throws IOException {
        Map<String, Object> headers = Collections.singletonMap("address", "address001");
        AMQP.BasicProperties properties = createProperties(headers);
        Consumer mockConsumer = mock(Consumer.class);
        arguments[6] = mockConsumer;
        TrafficUtils.setTrafficTag(new TrafficTag(new HashMap<>()));
        ExecuteContext context = buildContext(new Object(), arguments);
        interceptor.doBefore(context);
        ConsumerWrapper wrapper = (ConsumerWrapper) arguments[6];

        wrapper.handleDelivery("consumerTag", mock(Envelope.class), properties, new byte[0]);

        TrafficTag trafficTag = TrafficUtils.getTrafficTag();
        assertThat(trafficTag).isNotNull();
        assertThat(trafficTag.getTag()).isEmpty();
    }

    @Test
    public void testExtractMixedTags() throws IOException {
        Map<String, Object> headers = new HashMap<>();
        headers.put("name", "testName001");
        headers.put("address", "address001");
        AMQP.BasicProperties properties = createProperties(headers);
        Consumer mockConsumer = mock(Consumer.class);
        arguments[6] = mockConsumer;
        ExecuteContext context = buildContext(new Object(), arguments);
        interceptor.doBefore(context);
        ConsumerWrapper wrapper = (ConsumerWrapper) arguments[6];

        wrapper.handleDelivery("consumerTag", mock(Envelope.class), properties, new byte[0]);

        TrafficTag trafficTag = TrafficUtils.getTrafficTag();
        assertThat(trafficTag).isNotNull();
        assertThat(trafficTag.getTag()).containsEntry("name", Collections.singletonList("testName001"));
        assertThat(trafficTag.getTag()).doesNotContainKey("address");
    }

    @Test
    public void testExtractNullValue() throws IOException {
        Map<String, Object> headers = Collections.singletonMap("id", null);
        AMQP.BasicProperties properties = createProperties(headers);
        Consumer mockConsumer = mock(Consumer.class);
        arguments[6] = mockConsumer;
        ExecuteContext context = buildContext(new Object(), arguments);
        interceptor.doBefore(context);
        ConsumerWrapper wrapper = (ConsumerWrapper) arguments[6];

        wrapper.handleDelivery("consumerTag", mock(Envelope.class), properties, new byte[0]);

        TrafficTag trafficTag = TrafficUtils.getTrafficTag();
        assertThat(trafficTag).isNotNull();
        assertThat(trafficTag.getTag()).containsEntry("id", Collections.singletonList(null));
    }

    @Test
    public void testExtractWithNullProperties() throws IOException {
        Consumer mockConsumer = mock(Consumer.class);
        arguments[6] = mockConsumer;
        ExecuteContext context = buildContext(new Object(), arguments);
        interceptor.doBefore(context);
        ConsumerWrapper wrapper = (ConsumerWrapper) arguments[6];

        wrapper.handleDelivery("consumerTag", mock(Envelope.class), null, new byte[0]);

        TrafficTag trafficTag = TrafficUtils.getTrafficTag();
        assertThat(trafficTag).isNotNull();
        assertThat(trafficTag.getTag()).isEmpty();
    }

    @Test
    public void testExtractWithNullHeaders() throws IOException {
        AMQP.BasicProperties properties = createProperties(null);
        Consumer mockConsumer = mock(Consumer.class);
        arguments[6] = mockConsumer;
        ExecuteContext context = buildContext(new Object(), arguments);
        interceptor.doBefore(context);
        ConsumerWrapper wrapper = (ConsumerWrapper) arguments[6];

        wrapper.handleDelivery("consumerTag", mock(Envelope.class), properties, new byte[0]);

        TrafficTag trafficTag = TrafficUtils.getTrafficTag();
        assertThat(trafficTag).isNotNull();
        assertThat(trafficTag.getTag()).isEmpty();
    }

    private AMQP.BasicProperties createProperties(Map<String, Object> headers) {
        return new AMQP.BasicProperties.Builder()
            .headers(headers)
            .build();
    }

    protected ExecuteContext buildContext(Object object, Object[] arguments) {
        return ExecuteContext.forMemberMethod(object, null, arguments, null, null);
    }
}
