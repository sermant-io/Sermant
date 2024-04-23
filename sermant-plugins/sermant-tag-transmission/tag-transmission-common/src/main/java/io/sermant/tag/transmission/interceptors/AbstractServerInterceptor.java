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

package io.sermant.tag.transmission.interceptors;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.tag.transmission.config.TagTransmissionConfig;

import java.util.List;
import java.util.Map;

/**
 * The server-side interceptor abstracts class, gets the cross-process traffic tag and transmits it to the local
 * process，applicable to http client/rpc client/message queue consumer.
 *
 * @param <C> tag carrier
 * @author lilai
 * @since 2023-07-18
 */
public abstract class AbstractServerInterceptor<C> extends AbstractInterceptor {
    /**
     * Traffic tag transparent transmission configuration class
     */
    protected final TagTransmissionConfig tagTransmissionConfig;

    /**
     * constructor
     */
    public AbstractServerInterceptor() {
        this.tagTransmissionConfig = PluginConfigManager.getPluginConfig(TagTransmissionConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (!tagTransmissionConfig.isEffect()) {
            return context;
        }
        return doBefore(context);
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return doAfter(context);
    }

    /**
     * preTriggerPoint
     *
     * @param context execute context
     * @return ExecuteContext
     */
    protected abstract ExecuteContext doBefore(ExecuteContext context);

    /**
     * postTriggerPoint
     *
     * @param context execute context
     * @return ExecuteContext
     */
    protected abstract ExecuteContext doAfter(ExecuteContext context);

    /**
     * parse the traffic tag from the carrier
     *
     * @param carrier 载体
     * @return 流量标签
     */
    protected abstract Map<String, List<String>> extractTrafficTagFromCarrier(C carrier);
}
