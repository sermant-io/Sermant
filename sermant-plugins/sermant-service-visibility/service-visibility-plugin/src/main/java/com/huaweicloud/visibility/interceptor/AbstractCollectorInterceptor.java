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

import com.huaweicloud.sermant.core.common.BootArgsIndexer;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.visibility.common.ServiceType;
import com.huaweicloud.visibility.entity.Consanguinity;
import com.huaweicloud.visibility.entity.Contract;
import com.huaweicloud.visibility.entity.MethodInfo;
import com.huaweicloud.visibility.entity.ParamInfo;
import com.huaweicloud.visibility.service.CollectorService;

import org.springframework.cloud.client.ServiceInstance;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 信息采集服务
 *
 * @author zhp
 * @since 2022-11-30
 */
public abstract class AbstractCollectorInterceptor extends AbstractInterceptor {
    private static final String SEPARATOR = ",";

    /**
     * 消息发送服务
     */
    protected final CollectorService collectorService = PluginServiceManager.getPluginService(CollectorService.class);

    /**
     * 保存方法信息
     *
     * @param methodName 方法名称
     * @param interfaceClass 接口信息
     * @param contract 契约信息
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
     * 保存参数信息
     *
     * @param method 方法
     * @param methodInfo 方法信息存储类
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
     * 保存返回值信息
     *
     * @param method 方法
     * @param methodInfo 方法信息存储类
     */
    public void fillReturnInfo(Method method, MethodInfo methodInfo) {
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setParamType(method.getReturnType().getTypeName());
        methodInfo.setReturnInfo(paramInfo);
    }

    /**
     * 获取服务提供者信息
     *
     * @param serverList 服务提供者列表
     * @return 服务提供者信息
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
