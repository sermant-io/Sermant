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

package io.sermant.visibility.interceptor;

import io.sermant.core.common.BootArgsIndexer;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.core.utils.StringUtils;
import io.sermant.visibility.common.ServiceType;
import io.sermant.visibility.entity.Consanguinity;
import io.sermant.visibility.entity.Contract;
import io.sermant.visibility.entity.MethodInfo;
import io.sermant.visibility.entity.ParamInfo;
import io.sermant.visibility.service.CollectorService;

import org.springframework.cloud.client.ServiceInstance;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Information Collection Services
 *
 * @author zhp
 * @since 2022-11-30
 */
public abstract class AbstractCollectorInterceptor extends AbstractInterceptor {
    private static final String SEPARATOR = ",";

    /**
     * Messaging services
     */
    protected final CollectorService collectorService = PluginServiceManager.getPluginService(CollectorService.class);

    /**
     * Save method information
     *
     * @param methodName The name of the method
     * @param interfaceClass Interface information
     * @param contract Contract Information
     */
    public void fillMethodInfo(String methodName, Class<?> interfaceClass, Contract contract) {
        List<String> methodNames = Arrays.asList(methodName.split(SEPARATOR));
        Method[] methods = interfaceClass.getMethods();
        for (Method method : methods) {
            if (!method.isDefault() && methodNames.contains(method.getName())) {
                MethodInfo methodInfo = new MethodInfo();
                methodInfo.setName(method.getName());
                fillParamInfo(method, methodInfo);
                fillReturnInfo(method, methodInfo);
                contract.getMethodInfoList().add(methodInfo);
            }
        }
    }

    /**
     * Save the parameter information
     *
     * @param method method
     * @param methodInfo Method information storage class
     */
    public void fillParamInfo(Method method, MethodInfo methodInfo) {
        if (method.getParameters() == null) {
            return;
        }
        List<ParamInfo> paramInfoList = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            ParamInfo paramInfo = new ParamInfo();
            paramInfo.setParamType(parameter.getType().getTypeName());
            paramInfo.setParamName(parameter.getName());
            paramInfoList.add(paramInfo);
        }
        methodInfo.setParamInfoList(paramInfoList);
    }

    /**
     * Save the return value information
     *
     * @param method method
     * @param methodInfo Method information storage class
     */
    public void fillReturnInfo(Method method, MethodInfo methodInfo) {
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setParamType(method.getReturnType().getTypeName());
        methodInfo.setReturnInfo(paramInfo);
    }

    /**
     * Get service provider information
     *
     * @param serverList List of service providers
     * @return Service Provider Information
     */
    public static Consanguinity getConsanguinity(List<ServiceInstance> serverList) {
        Consanguinity consanguinity = new Consanguinity();
        consanguinity.setProviders(new ArrayList<>());
        consanguinity.setServiceType(ServiceType.SPRING_CLOUD.getType());
        consanguinity.setServiceKey(BootArgsIndexer.getInstanceId());
        consanguinity.setInterfaceName("");
        if (serverList != null && !serverList.isEmpty()) {
            serverList.forEach(server -> {
                Contract contract = new Contract();
                contract.setServiceType(ServiceType.SPRING_CLOUD.getType());
                contract.setIp(server.getHost());
                contract.setUrl(server.getUri().getPath());
                contract.setPort(StringUtils.getString(server.getPort()));
                contract.setServiceKey(server.getInstanceId());
                consanguinity.getProviders().add(contract);
            });
        }
        return consanguinity;
    }
}
