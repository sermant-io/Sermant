/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.config.grace.GraceContext;
import com.huawei.registry.utils.RefreshUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.util.List;

/**
 * 针对RestTemplate拦截
 *
 * @author zhouss
 * @since 2022-05-23
 */
public class SpringLoadbalancerRestTemplateResponseInterceptor extends GraceSwitchInterceptor {
    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        HttpRequest request = (HttpRequest) context.getArguments()[0];
        HttpHeaders headers = request.getHeaders();
        headers.putAll(getGraceIpHeaders());
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        final Object result = context.getResult();
        final Object argument = context.getArguments()[0];
        if (!(result instanceof ClientHttpResponse) || !(argument instanceof HttpRequest)) {
            return context;
        }
        ClientHttpResponse response = (ClientHttpResponse) result;
        final List<String> endpoints = response.getHeaders().get(GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT);
        if (endpoints == null || endpoints.isEmpty()) {
            return context;
        }
        GraceContext.INSTANCE.getGraceShutDownManager().addShutdownEndpoint(endpoints.get(0));
        HttpRequest request = (HttpRequest) argument;
        final String host = request.getURI().getHost();
        RefreshUtils.refreshTargetServiceInstances(host,
                response.getHeaders().get(GraceConstants.MARK_SHUTDOWN_SERVICE_NAME));
        return context;
    }

    @Override
    protected boolean isEnabled() {
        return super.isEnabled() && graceConfig.isEnableGraceShutdown();
    }
}
