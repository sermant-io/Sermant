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

package com.huaweicloud.sermant.tag.transmission.apachedubbov2.interceptors;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.CollectionUtils;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractClientInterceptor;

import org.apache.dubbo.rpc.RpcInvocation;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * dubbo流量标签透传consumer端的拦截器，支持dubbo2.7.x
 *
 * @author daizhenyu
 * @since 2023-08-12
 **/
public class ApacheDubboConsumerInterceptor extends AbstractClientInterceptor<RpcInvocation> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * rpcInvocation参数在invoke方法的参数下标
     */
    private static final int ARGUMENT_INDEX = 1;

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        Object invocationObject = context.getArguments()[ARGUMENT_INDEX];
        if (!(invocationObject instanceof RpcInvocation)) {
            return context;
        }
        injectTrafficTag2Carrier((RpcInvocation) invocationObject);
        return context;
    }

    /**
     * 向RpcInvocation中添加流量标签
     *
     * @param invocation Apache Dubbo 标签传递载体
     */
    @Override
    protected void injectTrafficTag2Carrier(RpcInvocation invocation) {
        for (Map.Entry<String, List<String>> entry : TrafficUtils.getTrafficTag().getTag().entrySet()) {
            String key = entry.getKey();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            List<String> values = entry.getValue();

            // provider端在标签值不为null的情况下转为list存储，为null时直接put null，因此在consumer端values为空必定是null
            if (CollectionUtils.isEmpty(values)) {
                invocation.setAttachment(key, null);
                LOGGER.log(Level.FINE, "Traffic tag {0} have been injected to dubbo.", entry);
                continue;
            }
            invocation.setAttachment(key, values.get(0));
            LOGGER.log(Level.FINE, "Traffic tag {0}={1} have been injected to dubbo.", new Object[]{key,
                    values.get(0)});
        }
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }
}
