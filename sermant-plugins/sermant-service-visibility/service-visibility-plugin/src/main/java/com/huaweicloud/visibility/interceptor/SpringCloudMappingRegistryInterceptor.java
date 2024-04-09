/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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
import com.huaweicloud.visibility.common.CollectorCache;
import com.huaweicloud.visibility.common.OperateType;
import com.huaweicloud.visibility.common.ServiceType;
import com.huaweicloud.visibility.entity.Contract;
import com.huaweicloud.visibility.entity.MethodInfo;
import com.huaweicloud.visibility.entity.ServerInfo;

import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * SpringCloud registration enhancer
 *
 * @author zhp
 * @since 2022-12-05
 */
public class SpringCloudMappingRegistryInterceptor extends AbstractCollectorInterceptor {
    private static final int ARGS_NUM = 3;

    private static final String SEPARATOR = ".";

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (context.getArguments() == null || context.getArguments().length < ARGS_NUM) {
            return context;
        }
        Contract contract = new Contract();
        for (Object object : context.getArguments()) {
            if (object instanceof RequestMappingInfo) {
                RequestMappingInfo requestMappingInfo = (RequestMappingInfo) object;
                contract.setUrl((String) requestMappingInfo.getPatternsCondition().getPatterns().toArray()[0]);
            }
            if (object instanceof Method) {
                Method method = (Method) object;
                MethodInfo methodInfo = new MethodInfo();
                methodInfo.setName(method.getName());
                fillParamInfo(method, methodInfo);
                fillReturnInfo(method, methodInfo);
                contract.setInterfaceName(method.getDeclaringClass().getName());
                contract.setMethodInfoList(new ArrayList<>());
                contract.getMethodInfoList().add(methodInfo);
                contract.setServiceKey(contract.getInterfaceName() + SEPARATOR + method.getName());
            }
        }
        contract.setServiceType(ServiceType.SPRING_CLOUD.getType());
        CollectorCache.saveContractInfo(contract);
        ServerInfo serverinfo = new ServerInfo();
        serverinfo.setContractList(new ArrayList<>());
        serverinfo.getContractList().add(contract);
        serverinfo.setOperateType(OperateType.ADD.getType());
        collectorService.sendServerInfo(serverinfo);
        return context;
    }
}
