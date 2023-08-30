/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.interceptors.rpc.servicecomb;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.CollectionUtils;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractClientInterceptor;

import org.apache.servicecomb.core.Invocation;

import java.util.List;

/**
 * servicecombRPC consumer端interceptor，支持servicecomb2.x版本
 *
 * @author daizhenyu
 * @since 2023-08-26
 **/
public class ServiceCombRpcConsumerInterceptor extends AbstractClientInterceptor<Invocation> {
    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments == null || arguments.length == 0) {
            return context;
        }
        Object invocationObject = arguments[0];
        if (invocationObject instanceof Invocation) {
            injectTrafficTag2Carrier((Invocation) invocationObject);
        }
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * 向Invocation中添加流量标签
     *
     * @param invocation servicecomb rpc客服端 标签传递载体
     */
    @Override
    protected void injectTrafficTag2Carrier(Invocation invocation) {
        for (String key : tagTransmissionConfig.getTagKeys()) {
            List<String> values = TrafficUtils.getTrafficTag().getTag().get(key);
            if (CollectionUtils.isEmpty(values)) {
                continue;
            }
            invocation.getContext().put(key, values.get(0));
        }
    }
}
