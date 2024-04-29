/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/dubbo/rpc/protocol/dubbo/DubboInvoker.java
 * from the Apache Dubbo project.
 */

package io.sermant.registry.grace.interceptors;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.registry.config.grace.GraceConstants;
import io.sermant.registry.config.grace.GraceContext;
import io.sermant.registry.utils.RefreshUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Interception for RestTemplate
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
        GraceContext.INSTANCE.getGraceShutDownManager()
                .addShutdownEndpoints(response.getHeaders().get(GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT));
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
