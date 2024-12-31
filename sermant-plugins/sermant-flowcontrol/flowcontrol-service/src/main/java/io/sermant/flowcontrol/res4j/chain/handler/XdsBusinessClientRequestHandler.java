/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.flowcontrol.res4j.chain.handler;

import io.sermant.flowcontrol.common.core.match.XdsRouteMatchManager;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.common.util.XdsThreadLocalUtil;
import io.sermant.flowcontrol.res4j.chain.AbstractChainHandler;
import io.sermant.flowcontrol.res4j.chain.HandlerConstants;
import io.sermant.flowcontrol.res4j.chain.context.RequestContext;

/**
 * Business handler class for client requests, Get the matched scenario information based on the routing match rule
 *
 * @author zhp
 * @since 2024-12-05
 */
public class XdsBusinessClientRequestHandler extends AbstractChainHandler {
    @Override
    public void onBefore(RequestContext context, FlowControlScenario scenario) {
        FlowControlScenario matchedScenario = XdsRouteMatchManager.INSTANCE.getMatchedScenarioInfo(
                context.getRequestEntity(), context.getRequestEntity().getServiceName());
        context.save(MATCHED_SCENARIO_NAMES, matchedScenario);
        XdsThreadLocalUtil.setScenarioInfo(matchedScenario);
        super.onBefore(context, matchedScenario);
    }

    @Override
    public void onThrow(RequestContext context, FlowControlScenario scenario, Throwable throwable) {
        super.onThrow(context, scenario, throwable);
    }

    @Override
    public void onResult(RequestContext context, FlowControlScenario scenario, Object result) {
        try {
            super.onResult(context, scenario, result);
        } finally {
            XdsThreadLocalUtil.removeScenarioInfo();
        }
    }

    @Override
    public int getOrder() {
        return HandlerConstants.XDS_BUSINESS_ORDER;
    }

    @Override
    protected boolean isSkip(RequestContext context, FlowControlScenario scenario) {
        return !XDS_FLOW_CONTROL_CONFIG.isEnable();
    }

    @Override
    protected RequestEntity.RequestType direct() {
        return RequestEntity.RequestType.CLIENT;
    }
}
