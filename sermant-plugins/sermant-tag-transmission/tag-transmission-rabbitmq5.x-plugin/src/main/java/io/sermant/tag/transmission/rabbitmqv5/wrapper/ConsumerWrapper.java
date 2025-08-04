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

package io.sermant.tag.transmission.rabbitmqv5.wrapper;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;

/**
 * consumer wrapper
 *
 * @since 2025-07-15
 */
public class ConsumerWrapper implements Consumer {

    private final Consumer delegate;
    private final java.util.function.Consumer<AMQP.BasicProperties> updateTagFunction;

    /**
     * constructor
     *
     * @param delegate consumer delegate
     * @param updateTagFunction update tag function
     */
    public ConsumerWrapper(Consumer delegate, java.util.function.Consumer<AMQP.BasicProperties> updateTagFunction) {
        this.delegate = delegate;
        this.updateTagFunction = updateTagFunction;
    }

    /**
     *  Called when a basic.deliver is received for this consumer.
     */
    @Override
    public void handleDelivery(final String consumerTag,
                               final Envelope envelope,
                               final AMQP.BasicProperties properties,
                               final byte[] body) throws IOException {
        // update tag then proxy
        updateTagFunction.accept(properties);
        this.delegate.handleDelivery(consumerTag, envelope, properties, body);
    }

    @Override
    public void handleConsumeOk(final String consumerTag) {
        this.delegate.handleConsumeOk(consumerTag);
    }

    @Override
    public void handleCancelOk(final String consumerTag) {
        this.delegate.handleCancelOk(consumerTag);
    }

    @Override
    public void handleCancel(final String consumerTag) throws IOException {
        this.delegate.handleCancel(consumerTag);
    }

    @Override
    public void handleShutdownSignal(final String consumerTag, final ShutdownSignalException sig) {
        this.delegate.handleShutdownSignal(consumerTag, sig);
    }

    @Override
    public void handleRecoverOk(final String consumerTag) {
        this.delegate.handleRecoverOk(consumerTag);
    }
}
