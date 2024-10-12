/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.discovery.entity;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.discovery.config.LbConfig;
import io.sermant.discovery.retry.config.DefaultRetryConfig;
import io.sermant.discovery.retry.config.RetryConfig;
import io.sermant.discovery.utils.HttpConstants;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpConversation;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.Response.CompleteListener;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * jetty client wrapper class
 *
 * @author provenceee
 * @since 2023-05-12
 */
public class JettyClientWrapper extends HttpRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final RetryConfig RETRY_CONFIG = DefaultRetryConfig.create();

    private static final LbConfig LB_CONFIG = PluginConfigManager.getPluginConfig(LbConfig.class);

    private static final String ABORTED_FIELD_NAME = "aborted";

    private final String originHost;

    private final int originPort;

    private final String originPath;

    private CompleteListener originCompleteListener;

    private int retryTimes;

    /**
     * Constructor
     *
     * @param client client
     * @param conversation conversation
     * @param uri uri
     */
    public JettyClientWrapper(HttpClient client, HttpConversation conversation, URI uri) {
        super(client, conversation, uri);
        this.originHost = super.getHost();
        this.originPort = super.getPort();
        this.originPath = super.getPath();
    }

    @Override
    public void send(CompleteListener listener) {
        if (listener != null) {
            this.originCompleteListener = listener;
        }
        super.send(listener);
    }

    @Override
    public boolean abort(Throwable throwable) {
        boolean shouldRetry = beforeAbort(throwable);
        boolean abort = super.abort(throwable);
        afterAbort(shouldRetry);
        return abort;
    }

    private boolean beforeAbort(Throwable throwable) {
        boolean shouldRetry =
                retryTimes < RETRY_CONFIG.getMaxRetry() && RETRY_CONFIG.getThrowablePredicate().test(throwable);
        if (shouldRetry) {
            LOGGER.log(Level.WARNING, "Start retry, throwable is: [{0}], is from [{1}].",
                    new Object[]{throwable, getHost()});
            try {
                Thread.sleep(LB_CONFIG.getRetryWaitMs());
            } catch (InterruptedException ignored) {
                // ignored
            }

            // Remove the original listener to prevent error callbacks
            HttpConversation httpConversation = getConversation();
            httpConversation.getResponseListeners().removeIf(listener -> listener == originCompleteListener);
        }
        return shouldRetry;
    }

    private void afterAbort(boolean shouldRetry) {
        if (!shouldRetry) {
            return;
        }

        // Clear the original request
        HttpConversation httpConversation = getConversation();
        httpConversation.getExchanges().clear();
        httpConversation.getResponseListeners().clear();

        // Request information such as restoring a domain name
        ReflectUtils.setFieldValue(this, HttpConstants.HTTP_URI_HOST, originHost);
        ReflectUtils.setFieldValue(this, HttpConstants.HTTP_URI_PORT, originPort);
        ReflectUtils.setFieldValue(this, HttpConstants.HTTP_URI_PATH, originPath);
        Optional<Object> aborted = ReflectUtils.getFieldValue(this, ABORTED_FIELD_NAME);
        aborted.ifPresent(obj -> ((AtomicReference<?>) obj).set(null));

        // Re-request
        send(null);
        retryTimes++;
    }
}
