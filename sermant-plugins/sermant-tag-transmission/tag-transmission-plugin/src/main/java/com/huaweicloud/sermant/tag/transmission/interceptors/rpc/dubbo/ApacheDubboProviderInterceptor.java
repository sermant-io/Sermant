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

package com.huaweicloud.sermant.tag.transmission.interceptors.rpc.dubbo;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractServerInterceptor;
import com.huaweicloud.sermant.tag.transmission.utils.DubboUtils;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcInvocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * dubbo流量标签透传的provider端拦截器，支持dubbo2.7.x, 3.x
 *
 * @author daizhenyu
 * @since 2023-08-02
 **/
public class ApacheDubboProviderInterceptor extends AbstractServerInterceptor<RpcInvocation> {
    /**
     * invoker参数在invoke方法的参数下标
     */
    private static final int ARGUMENT_INVOKER_INDEX = 0;

    /**
     * invocation参数在invoke方法的参数下标
     */
    private static final int ARGUMENT_INVOCATION_INDEX = 1;

    /**
     * dubbo客户端
     */
    private static final String DUBBO_CONSUMER = "consumer";

    /**
     * dubbo服务端
     */
    private static final String DUBBO_PROVIDER = "provider";

    /**
     * 区分dubbo调用端 provider 服务端 consumer 客户端
     */
    private static final String DUBBO_SIDE = "side";

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        if (isConsumer(context)) {
            return context;
        }

        Object invocationArgument = context.getArguments()[ARGUMENT_INVOCATION_INDEX];
        if (!(invocationArgument instanceof RpcInvocation)) {
            return context;
        }

        TrafficUtils.updateTrafficTag(extractTrafficTagFromCarrier((RpcInvocation) invocationArgument));
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        TrafficUtils.removeTrafficTag();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        TrafficUtils.removeTrafficTag();
        return context;
    }

    /**
     * 从RpcInvocation中解析流量标签
     *
     * @param invocation Apache Dubbo服务端的流量标签载体
     * @return 流量标签
     */
    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(RpcInvocation invocation) {
        Map<String, List<String>> tag = new HashMap<>();

        // 适配不同版本apache dubbo的RpcInvocation的attachments属性泛型不一致情况
        Map<String, Object> attachments = DubboUtils.getAttachmentsByInvocation(invocation)
                .filter(obj -> obj instanceof Map)
                .map(obj -> (Map<String, Object>) obj)
                .orElse(new HashMap<>());

        Set<String> keySet = attachments.keySet();
        for (String key : keySet) {
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            Object value = attachments.get(key);
            if (value instanceof String) {
                tag.put(key, Collections.singletonList((String) value));
                continue;
            }

            // 流量标签的value为null或不为String对象时，也需存入本地变量，覆盖原来的value，以防误用旧流量标签
            tag.put(key, null);
        }
        return tag;
    }

    private boolean isConsumer(ExecuteContext context) {
        Object invokerArgument = context.getArguments()[ARGUMENT_INVOKER_INDEX];
        if (!(invokerArgument instanceof Invoker<?>)) {
            return false;
        }
        Invoker<?> invoker = (Invoker<?>) invokerArgument;
        return isConsumer(invoker);
    }

    private boolean isConsumer(Invoker<?> invoker) {
        return DUBBO_CONSUMER.equals(invoker.getUrl().getParameter(DUBBO_SIDE, DUBBO_PROVIDER));
    }
}