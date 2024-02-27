/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.service;

import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.request.RequestTag;
import com.huaweicloud.sermant.router.common.service.AbstractDirectoryService;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.DubboReflectUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.dubbo.handler.HandlerChainEntry;

import java.util.List;
import java.util.Map;

/**
 * AbstractDirectory的service
 *
 * @author provenceee
 * @since 2021-11-24
 */
public class AbstractDirectoryServiceImpl implements AbstractDirectoryService {
    // dubbo请求参数中是否为consumer的key值
    private static final String CONSUMER_KEY = "side";

    // dubbo请求参数中接口名的key值
    private static final String INTERFACE_KEY = "interface";

    // dubbo请求参数中是否为consumer的value值
    private static final String CONSUMER_VALUE = "consumer";

    /**
     * 筛选标签invoker
     *
     * @param registryDirectory RegistryDirectory
     * @param invocation 参数
     * @param result invokers
     * @return invokers
     * @see com.alibaba.dubbo.registry.integration.RegistryDirectory
     * @see org.apache.dubbo.registry.integration.RegistryDirectory
     * @see com.alibaba.dubbo.rpc.Invoker
     * @see org.apache.dubbo.rpc.Invoker
     */
    @Override
    public Object selectInvokers(Object registryDirectory, Object invocation, Object result) {
        if (invocation == null) {
            return result;
        }
        if (!(result instanceof List<?>)) {
            return result;
        }
        putAttachment(invocation);
        List<Object> invokers = (List<Object>) result;
        Map<String, String> queryMap = DubboReflectUtils.getQueryMap(registryDirectory);
        if (CollectionUtils.isEmpty(queryMap)) {
            return invokers;
        }
        if (!CONSUMER_VALUE.equals(queryMap.get(CONSUMER_KEY))) {
            return invokers;
        }
        String serviceInterface = queryMap.get(INTERFACE_KEY);
        String targetService = DubboCache.INSTANCE.getApplication(serviceInterface);
        if (StringUtils.isBlank(targetService)) {
            return invokers;
        }

        return HandlerChainEntry.INSTANCE.process(targetService, invokers, invocation, queryMap, serviceInterface);
    }

    private void putAttachment(Object invocation) {
        RequestTag requestTag = ThreadLocalUtils.getRequestTag();
        if (requestTag == null || CollectionUtils.isEmpty(requestTag.getTag())) {
            return;
        }
        Map<String, Object> attachments = DubboReflectUtils.getAttachmentsByInvocation(invocation);
        if (attachments != null) {
            requestTag.getTag().forEach((key, value) -> attachments.putIfAbsent(key, value.get(0)));
        }
    }
}