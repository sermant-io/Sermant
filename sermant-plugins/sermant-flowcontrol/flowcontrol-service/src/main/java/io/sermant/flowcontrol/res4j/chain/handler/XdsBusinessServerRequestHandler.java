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

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.flowcontrol.common.core.match.XdsRouteMatchManager;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;
import io.sermant.flowcontrol.common.util.XdsThreadLocalUtil;
import io.sermant.flowcontrol.res4j.chain.AbstractXdsChainHandler;
import io.sermant.flowcontrol.res4j.chain.HandlerConstants;

/**
 * Business handler class for server requests, Get the matched scenario information based on the routing match rule
 *
 * @author zhp
 * @since 2024-12-05
 */
public class XdsBusinessServerRequestHandler extends AbstractXdsChainHandler {
    private final ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);

    @Override
    public void onBefore(RequestEntity requestEntity, FlowControlScenario scenarioInfo) {
        FlowControlScenario matchedScenarioEntity = XdsRouteMatchManager.INSTANCE.getMatchedScenarioInfo(
                requestEntity, serviceMeta.getService());
        XdsThreadLocalUtil.setScenarioInfo(matchedScenarioEntity);
        super.onBefore(requestEntity, matchedScenarioEntity);
    }

    @Override
    public void onThrow(RequestEntity requestEntity, FlowControlScenario scenarioInfo, Throwable throwable) {
        if (scenarioInfo == null) {
            super.onThrow(requestEntity, XdsThreadLocalUtil.getScenarioInfo(), throwable);
            return;
        }
        super.onThrow(requestEntity, scenarioInfo, throwable);
    }

    @Override
    public void onAfter(RequestEntity requestEntity, FlowControlScenario scenarioInfo, Object result) {
        if (scenarioInfo == null) {
            super.onAfter(requestEntity, XdsThreadLocalUtil.getScenarioInfo(), result);
            return;
        }
        super.onAfter(requestEntity, scenarioInfo, result);
    }

    @Override
    public int getOrder() {
        return HandlerConstants.XDS_BUSINESS_ORDER;
    }

    @Override
    protected RequestType direct() {
        return RequestType.SERVER;
    }
}
