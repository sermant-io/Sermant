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

package io.sermant.flowcontrol.res4j.chain.handler;

import io.sermant.flowcontrol.common.core.match.MatchManager;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.res4j.chain.AbstractChainHandler;
import io.sermant.flowcontrol.res4j.chain.HandlerConstants;
import io.sermant.flowcontrol.res4j.chain.context.ChainContext;
import io.sermant.flowcontrol.res4j.chain.context.RequestContext;

import java.util.Set;

/**
 * The service matches the handler with the highest priority（in addition to surveillance）
 *
 * @author zhouss
 * @since 2022-07-05
 */
public class BusinessRequestHandler extends AbstractChainHandler {
    @Override
    public void onBefore(RequestContext context, FlowControlScenario scenarioInfo) {
        final Set<String> matchBusinessNames = MatchManager.INSTANCE.matchWithCache(context.getRequestEntity());
        if (scenarioInfo != null) {
            scenarioInfo.setMatchedScenarioNames(matchBusinessNames);
            super.onBefore(context, scenarioInfo);
        } else {
            FlowControlScenario flowControlScenario = new FlowControlScenario();
            flowControlScenario.setMatchedScenarioNames(matchBusinessNames);
            super.onBefore(context, flowControlScenario);
        }
    }

    @Override
    public void onThrow(RequestContext context, FlowControlScenario scenarioInfo, Throwable throwable) {
        super.onThrow(context, scenarioInfo, throwable);
    }

    @Override
    public void onResult(RequestContext context, FlowControlScenario scenarioInfo, Object result) {
        try {
            super.onResult(context, scenarioInfo, result);
        } finally {
            ChainContext.getThreadLocalContext(context.getSourceName()).remove(MATCHED_SCENARIO_NAMES);
        }
    }

    @Override
    public int getOrder() {
        return HandlerConstants.BUSINESS_ORDER;
    }
}
