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
import com.huaweicloud.sermant.tag.transmission.config.TagTransmissionConfig;

import java.util.List;
import java.util.Map;

/**
 * 服务端拦截器抽象类，获取跨进程的流量标签并在本进程传递，适用于http服务端/rpc服务端/消息队列消费者
 *
 * @param <Carrier> 流量标签载体
 * @author lilai
 * @since 2023-07-18
 */
public abstract class AbstractServerInterceptor<Carrier> extends AbstractInterceptor {
    /**
     * 过滤一次处理过程中拦截器的多次调用
     */
    protected static final ThreadLocal<Boolean> LOCK_MARK = new ThreadLocal<>();

    protected final TagTransmissionConfig tagTransmissionConfig;

    /**
     * 构造器
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

    /**
     * 从载体中解析流量标签
     *
     * @param carrier 载体
     * @return 流量标签
     */
    protected abstract Map<String, List<String>> extractTrafficTagFromCarrier(Carrier carrier);
}
