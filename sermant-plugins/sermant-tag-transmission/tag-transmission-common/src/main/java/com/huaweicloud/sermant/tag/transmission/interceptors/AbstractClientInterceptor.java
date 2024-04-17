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

package com.huaweicloud.sermant.tag.transmission.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.MapUtils;
import com.huaweicloud.sermant.core.utils.tag.TrafficTag;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.TagTransmissionConfig;

/**
 * The client interceptor abstract class takes the current thread's traffic tag and transmits it transparently to
 * downstream processes，applicable to http client/rpc client/message queue producer.
 *
 * @param <C> tag carrier
 * @author lilai
 * @since 2023-07-18
 */
public abstract class AbstractClientInterceptor<C> extends AbstractInterceptor {
    /**
     * Traffic tag transparent transmission configuration class
     */
    protected final TagTransmissionConfig tagTransmissionConfig;

    /**
     * constructor
     */
    public AbstractClientInterceptor() {
        this.tagTransmissionConfig = PluginConfigManager.getPluginConfig(TagTransmissionConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (!tagTransmissionConfig.isEffect()) {
            return context;
        }

        TrafficTag trafficTag = TrafficUtils.getTrafficTag();
        if (trafficTag == null || MapUtils.isEmpty(trafficTag.getTag())) {
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
     * the traffic tag is injected into the carrier
     *
     * @param carrier carrier
     */
    protected abstract void injectTrafficTag2Carrier(C carrier);
}
