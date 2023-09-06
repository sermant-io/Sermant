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
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractServerInterceptor;

import io.grpc.Metadata;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * grpc 服务端拦截器
 *
 * @author daizhenyu
 * @since 2023-08-15
 **/
public class GrpcServerInterceptor extends AbstractServerInterceptor<Metadata> {
    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        Object object = context.getObject();
        if (object == null) {
            return context;
        }
        if (object instanceof ServerBuilder) {
            ServerBuilder<?> builder = (ServerBuilder) object;
            ServerInterceptor interceptor = new ServerHeaderInterceptor();
            builder.intercept(interceptor);
        }
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(Metadata metadata) {
        return new HashMap<>();
    }
}
