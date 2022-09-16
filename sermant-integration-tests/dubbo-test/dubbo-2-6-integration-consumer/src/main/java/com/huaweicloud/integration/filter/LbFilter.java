/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.integration.filter;

import com.huaweicloud.integration.configuration.LbCache;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.support.RpcUtils;

/**
 * 负载均衡过滤器, 通过此处来获取实际拿到的负载均衡
 *
 * @author zhouss
 * @since 2022-09-16
 */
@Activate(group = {Constants.CONSUMER})
public class LbFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if ("lb".equals(invocation.getMethodName())) {
            LbCache.INSTANCE.setLb(invoker.getUrl().getMethodParameter(RpcUtils.getMethodName(invocation),
                    Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE));
        }
        return invoker.invoke(invocation);
    }
}
