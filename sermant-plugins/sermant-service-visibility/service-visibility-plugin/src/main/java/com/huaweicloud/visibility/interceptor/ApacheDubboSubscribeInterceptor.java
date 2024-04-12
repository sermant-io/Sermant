/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.visibility.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.visibility.common.CollectorCache;
import com.huaweicloud.visibility.common.Constants;
import com.huaweicloud.visibility.common.OperateType;
import com.huaweicloud.visibility.common.ServiceType;
import com.huaweicloud.visibility.entity.Consanguinity;
import com.huaweicloud.visibility.entity.Contract;
import com.huaweicloud.visibility.entity.ServerInfo;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invoker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dubbo Service Discovery Enhancer
 *
 * @author zhp
 * @since 2022-12-05
 */
public class ApacheDubboSubscribeInterceptor extends AbstractCollectorInterceptor {
    private static final String INVOKE_FIELD_NAME = "urlInvokerMap";

    private static final String PROVIDER_URL_FIELD_NAME = "providerUrl";

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        Object object = context.getMemberFieldValue(INVOKE_FIELD_NAME);
        if (!(object instanceof Map)) {
            return context;
        }
        Map<String, Invoker<?>> urlInvokerMap =
                (Map<String, Invoker<?>>) context.getMemberFieldValue(INVOKE_FIELD_NAME);
        if (urlInvokerMap == null || urlInvokerMap.isEmpty()) {
            return context;
        }
        ServerInfo serverinfo = new ServerInfo();
        extracted(urlInvokerMap, serverinfo);
        serverinfo.setOperateType(OperateType.ADD.getType());
        if (!serverinfo.getConsanguinityList().isEmpty()) {
            serverinfo.getConsanguinityList().forEach(CollectorCache::saveConsanguinity);
        } else {
            List<URL> invokerUrls = (List<URL>) context.getArguments()[0];
            if (invokerUrls != null && invokerUrls.size() > 0) {
                invokerUrls.forEach(invokerUrl -> {
                    Consanguinity consanguinity = fillConsanguinity(invokerUrl);
                    serverinfo.getConsanguinityList().add(consanguinity);
                    CollectorCache.saveConsanguinity(consanguinity);
                });
            }
        }
        collectorService.sendServerInfo(serverinfo);
        return context;
    }

    /**
     * Get kinship information
     *
     * @param urlInvokerMap Service Provider Information
     * @param serverinfo Service Information
     */
    private static void extracted(Map<String, Invoker<?>> urlInvokerMap, ServerInfo serverinfo) {
        Map<String, Consanguinity> consanguinityMap = new HashMap<>();
        serverinfo.setConsanguinityList(new ArrayList<>());
        for (Map.Entry<String, Invoker<?>> entry : urlInvokerMap.entrySet()) {
            Invoker<?> invoker = entry.getValue();
            Optional<Object> optional = ReflectUtils.getFieldValue(invoker, PROVIDER_URL_FIELD_NAME);
            if (optional.isPresent()) {
                URL url = invoker.getUrl();
                Consanguinity consanguinity;
                if (consanguinityMap.get(url.getServiceKey()) != null) {
                    consanguinity = consanguinityMap.get(url.getServiceKey());
                } else {
                    consanguinity = fillConsanguinity(url);
                    consanguinityMap.put(url.getServiceKey(), consanguinity);
                }
                Contract contract = new Contract();
                URL providerUrl = (URL) optional.get();
                contract.setUrl(providerUrl.getPath());
                contract.setIp(providerUrl.getHost());
                contract.setPort(StringUtils.getString(providerUrl.getPort()));
                contract.setServiceKey(providerUrl.getServiceKey());
                contract.setServiceType(ServiceType.DUBBO.getType());
                consanguinity.getProviders().add(contract);
                serverinfo.getConsanguinityList().add(consanguinity);
            }
        }
    }

    /**
     * Kinship filling method
     *
     * @param invokerUrl URL information
     * @return Populated with kinship information
     */
    private static Consanguinity fillConsanguinity(URL invokerUrl) {
        Consanguinity consanguinity = new Consanguinity();
        consanguinity.setServiceType(ServiceType.DUBBO.getType());
        consanguinity.setUrl(invokerUrl.getPath());
        consanguinity.setIp(invokerUrl.getHost());
        consanguinity.setPort(StringUtils.getString(invokerUrl.getPort()));
        consanguinity.setInterfaceName(invokerUrl.getParameter(Constants.INTERFACE_FIELD_NAME));
        consanguinity.setServiceKey(invokerUrl.getServiceKey());
        consanguinity.setProviders(new ArrayList<>());
        return consanguinity;
    }
}
