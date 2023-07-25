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
 * 客户端拦截器抽象类，获取当前线程的流量标签并透传至下游进程，适用于http客户端/rpc客户端/消息队列生产者
 *
 * @author lilai
 * @since 2023-07-18
 */
public abstract class AbstractClientInterceptor extends AbstractInterceptor {
    protected final TagTransmissionConfig tagTransmissionConfig;

    /**
     * 构造器
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
     * 前置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    protected abstract ExecuteContext doBefore(ExecuteContext context);

    /**
     * 后置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    protected abstract ExecuteContext doAfter(ExecuteContext context);
}
