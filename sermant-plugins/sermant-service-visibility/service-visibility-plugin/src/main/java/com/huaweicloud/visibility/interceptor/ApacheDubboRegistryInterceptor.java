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
import com.huaweicloud.sermant.core.service.visibility.common.CollectorCache;
import com.huaweicloud.sermant.core.service.visibility.common.OperateType;
import com.huaweicloud.sermant.core.service.visibility.common.ServiceType;
import com.huaweicloud.sermant.core.service.visibility.entity.Contract;
import com.huaweicloud.sermant.core.service.visibility.entity.ServerInfo;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.visibility.common.Constants;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dubbo注册增强方法
 *
 * @author zhp
 * @since 2022-11-30
 */
public class ApacheDubboRegistryInterceptor extends AbstractCollectorInterceptor {
    private static final String EXPORTER_FIELD_NAME = "exporters";

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        Optional<Object> optional = ReflectUtils.getFieldValue(context.getObject(), EXPORTER_FIELD_NAME);
        if (!optional.isPresent() || !(optional.get() instanceof List)) {
            return context;
        }
        List<Exporter<?>> exporters = (List<Exporter<?>>) optional.get();
        if (exporters.isEmpty()) {
            return context;
        }
        ServerInfo serverinfo = new ServerInfo();
        serverinfo.setContractList(new ArrayList<>());
        Map<String, Contract> contractMap = new HashMap<>();
        exporters.forEach(exporter -> {
            Invoker<?> invoker = exporter.getInvoker();
            URL url = invoker.getUrl();
            if (contractMap.get(url.getServiceKey()) == null) {
                Contract contract = new Contract();
                contract.setMethodInfoList(new ArrayList<>());
                contract.setServiceType(ServiceType.DUBBO.getType());
                contract.setIp(url.getParameter(Constants.IP_FIELD_NAME));
                contract.setPort(url.getParameter(Constants.PORT_FIELD_NAME));
                contract.setUrl(url.getPath());
                contract.setServiceKey(url.getServiceKey());
                Class<?> interfaceClass = invoker.getInterface();
                fillMethodInfo(url.getParameter(Constants.METHOD_FIELD_NAME), interfaceClass, contract);
                contract.setInterfaceName(invoker.getInterface().getCanonicalName());
                CollectorCache.saveContractInfo(contract);
                serverinfo.getContractList().add(contract);
                contractMap.put(url.getServiceKey(), contract);
            }
        });
        serverinfo.setOperateType(OperateType.ADD.getType());
        serverinfo.setRegistryInfo(CollectorCache.REGISTRY_MAP);
        collectorService.sendServerInfo(serverinfo);
        return context;
    }
}
