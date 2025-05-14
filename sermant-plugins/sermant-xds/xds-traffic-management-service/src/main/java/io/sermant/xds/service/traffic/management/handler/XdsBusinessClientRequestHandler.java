/*
 * Copyright (C) 2024-2025 Sermant Authors. All rights reserved.
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

package io.sermant.xds.service.traffic.management.handler;

import io.sermant.xds.common.context.XdsTrafficManagementContext;
import io.sermant.xds.common.entity.FlowControlScenario;
import io.sermant.xds.common.entity.RequestEntity;
import io.sermant.xds.common.entity.RequestEntity.RequestType;
import io.sermant.xds.common.match.XdsRouteMatchManager;
import io.sermant.xds.service.traffic.management.constant.HandlerConstants;

/**
 * Business handler class for client requests, Get the matched scenario information based on the routing match rule
 *
 * @author zhp
 * @since 2024-12-05
 */
public class XdsBusinessClientRequestHandler extends AbstractXdsChainHandler {
    @Override
    public void onBefore(RequestEntity requestEntity, FlowControlScenario scenario) {
        FlowControlScenario matchedScenario = XdsRouteMatchManager.INSTANCE.getMatchedScenarioInfo(
                requestEntity, requestEntity.getServiceName());
        XdsTrafficManagementContext.setScenarioInfo(matchedScenario);
        super.onBefore(requestEntity, matchedScenario);
    }

    @Override
    public void onThrow(RequestEntity requestEntity, FlowControlScenario scenario, Throwable throwable) {
        super.onThrow(requestEntity, scenario, throwable);
    }

    @Override
    public void onAfter(RequestEntity requestEntity, FlowControlScenario scenario, Object result) {
        super.onAfter(requestEntity, scenario, result);
    }

    @Override
    public int getOrder() {
        return HandlerConstants.XDS_BUSINESS_ORDER;
    }

    @Override
    protected RequestType direct() {
        return RequestType.CLIENT;
    }

    @Override
    protected boolean isSkip(RequestEntity requestEntity, FlowControlScenario flowControlScenario) {
        return false;
    }
}
