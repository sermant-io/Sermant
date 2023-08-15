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

package com.huaweicloud.sermant.tag.transmission.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficTag;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;

import com.alibaba.dubbo.rpc.RpcInvocation;

import java.util.List;
import java.util.Map;

/**
 * dubbo流量标签透传consumer端的拦截器，支持alibaba dubbo2.6.x版本
 *
 * @author daizhenyu
 * @since 2023-08-12
 **/
public class AlibabaDubboConsumerInterceptor extends AbstractClientInterceptor {
    /**
     * rpcInvocation参数在invoke方法的参数下标
     */
    private static final int ARGUMENT_INDEX = 1;

    /**
     * ApacheDubboV3ConsumerInterceptor类的无参构造方法
     */
    public AlibabaDubboConsumerInterceptor() {
    }

    private void addTag2Attachment(Object invocation, TrafficTag trafficTag) {
        if (invocation == null) {
            return;
        }
        if (invocation instanceof RpcInvocation) {
            RpcInvocation rpcInvocation = (RpcInvocation) invocation;
            addTag2Attachment(trafficTag.getTag(), rpcInvocation);
        }
    }

    private void addTag2Attachment(Map<String, List<String>> tag, RpcInvocation invocation) {
        for (Map.Entry<String, List<String>> entry : tag.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            invocation.setAttachment(entry.getKey(), entry.getValue().get(0));
        }
    }

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        this.addTag2Attachment(context.getArguments()[ARGUMENT_INDEX], TrafficUtils.getTrafficTag());
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }
}
