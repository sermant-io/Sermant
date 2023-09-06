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

package com.huaweicloud.sermant.tag.transmission.interceptors.rpc.grpc;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.CollectionUtils;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractClientInterceptor;

import io.grpc.Metadata;

import java.util.List;

/**
 * grpc使用DynamicMessage调用服务端，拦截header参数注入流量标签
 *
 * @author daizhenyu
 * @since 2023-08-21
 **/
public class ClientCallImplInterceptor extends AbstractClientInterceptor<Metadata> {
    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        Object[] arguments = context.getArguments();

        // 被拦截方法的入参数量为2
        if (arguments == null || arguments.length <= 1) {
            return context;
        }
        Object metadataObject = arguments[1];
        if (metadataObject instanceof Metadata) {
            injectTrafficTag2Carrier((Metadata) metadataObject);
        }
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    @Override
    protected void injectTrafficTag2Carrier(Metadata header) {
        for (String key : tagTransmissionConfig.getTagKeys()) {
            List<String> values = TrafficUtils.getTrafficTag().getTag().get(key);
            if (CollectionUtils.isEmpty(values)) {
                continue;
            }
            header.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), values.get(0));
        }
    }
}
